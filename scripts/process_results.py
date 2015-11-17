from re import compile
from sys import stdout, stderr
from math import ceil
from os.path import isfile
from os import listdir



plt=open("plot.plt","w")
plt.write("""# autogenerated plot script
set term pngcairo background "#ffffff" enhanced fontscale 1.2 size 800, 600 
set style data linesp
set colorsequence classic

set logscale xy
set clip point
set noclip one

set pointsize 1

""")

dat=open("plot.dat","w")
dat_input=0


nodesthreads_pattern = compile(r"(?P<name>\S+)\s+using:\s*(?P<nodes>\d+)\s+nodes\s*(?P<threads>\d+)\s+threads?")
result_pattern = compile(r"(?P<name>\S+)\s+(?P<cores>\d+)\s+(size\s+(?P<size>\d+(?:\.\d*)?)\s+)?time\s+(?P<time>\d+(?:\.\d*)?)")
result_benchmarks=sorted([name for name in listdir(".") if name.endswith(".out") and not name.startswith("pingpong")])
for filename in result_benchmarks:
    stderr.write("Processing file: %s..."%filename)
    if isfile(filename) == False:
        stderr.write(" file doesn't exist!\n")
        continue


    f=open(filename, "r")
    body = f.read()
    f.close()

    plt.write("\n\n### --- %s --- ###\n\n" % filename)

    nodes = None
    threads = None
    name = None
    s_nodes = set()
    s_cores = set()
    data = dict()
    for line in body.split("\n"):
        m = nodesthreads_pattern.match(line)
        if m is not None:
            if name is None: name = m.group('name')
            nodes = int(m.group('nodes'))
            threads = int(m.group('threads'))
            s_nodes.add(nodes)
        else:
            m = result_pattern.match(line)
            if m is not None:
                size = m.group('size')
                if size is not None: size = float(size)
                time = float(m.group('time'))
                cores = float(m.group('cores'))
                s_cores.add(cores)
                if cores != nodes*threads:
                    stderr.write("Error in %s: cores(%d) not equal nodes(%d)*threads(%d)\n" % (filename, cores, nodes,threads))
                if size not in data: data[size] = dict()
                if cores not in data[size]: data[size][cores] = dict()
                data[size][cores][nodes] = time

    l_nodes = sorted(s_nodes, reverse=False)
    l_cores = sorted(s_cores, reverse=False)
    l_xtics = l_cores[::int(ceil(len(l_cores)/4.))]
    if l_cores[-1] not in l_xtics: l_xtics.append(l_cores[-1])
    xtics = (", ".join(['"%d" %d' % (x,x) for x in l_xtics]))
    l_cores = None
    l_xtics = None

    for size,cores_values in sorted(data.items()):
        plt.write("# ---v--- input: %d ---v--- \n" % dat_input)

        if size is None:
            plot_name = name
        else:
            plot_name = "%s (%.7g KB)" % (name, size)
        dat.write("### Input %d: %s" % (dat_input,plot_name))
        dat.write("\n# Nproc\tt_min")
        for nodes in l_nodes:
            dat.write("\t%d"%nodes)
        dat.write("\n")

        for cores,values in sorted(cores_values.items()):
            min_value = float("inf")
            for nodes in l_nodes:
                if nodes in values:
                    if values[nodes]<min_value: min_value=values[nodes]
            dat.write("%d\t%.7f" % (cores,min_value))
            for nodes in l_nodes:
                if nodes not in values:
                    dat.write("\t-")
                else:
                    dat.write("\t%.7f" % values[nodes])
            dat.write("\n")
        dat.write("\n\n")

        plt.write("set title '%s'\n" % plot_name.replace('_','\_'))
        plt.write("set xlabel 'Thread count'\n")
        plt.write("set ylabel 'Time [s]'\n")
        plt.write("set format y ' 10^{%T}'\n")
        plt.write("set autoscale y\n")
        plt.write("set xrange [0.9:%.1f]\n" % (sorted(cores_values)[-1]*1.1))
        plt.write("set xtics (%s)\n" % xtics)
        plt.write("#set key nobox at %.3f,%.3f\n" % (sorted(cores_values)[-1],0.01))
        plt.write("set output '%s_%d.png'\n\n" % (filename[:-4], dat_input))

        #plt.write("plot 'plot.dat' i %d u 1:2 t 'min' w lp lt 9 lw 2 pt 0" % dat_input) # min
        col=3
        plt.write("plot 'plot.dat' i %d u 1:%d t '%d nodes' w lp lt %d lw 3 pt 7"%(
            dat_input,
            col, # column
            l_nodes[0], # nodes
            col-2 # color
        ))
        col = col+1
        for nodes in l_nodes[1:]:
            plt.write(",\\\n     'plot.dat' i %d u 1:%d t '%d nodes' w lp lt %d lw 3 pt 7"%(
                dat_input,
                col, # column
                nodes, # nodes
                col-2 # color
            ))
            col=col+1
        plt.write("\n\n")

        plt.write("# ---^--- input: %d ---^--- \n" % dat_input)
        dat_input = dat_input + 1

    stderr.write(" done\n")


pingpong_pattern = compile(r"(?P<name>\S+)\s+(?P<cores>\d+)\s+size\s+(?P<size>\d+(?:\.\d*)?)\s+t_get\s+(?P<t_get>\d+(?:\.\d*)?)\s+t_put\s+(?P<t_put>\d+(?:\.\d*)?)\s+t_putB\s+(?P<t_putB>\d+(?:\.\d*)?)")
pingpong_benchmarks=sorted([name for name in listdir(".") if name.endswith(".out") and name.startswith("pingpong")])
for filename in pingpong_benchmarks:
    stderr.write("Processing file: %s..."%filename)
    if isfile(filename) == False:
        stderr.write(" file doesn't exist!\n")
        continue

    f=open(filename, "r")
    body = f.read()
    f.close()

    plt.write("\n\n### --- %s --- ###\n\n" % filename)


    name = None
    s_names = set()
    s_sizes = set()
    data = dict()
    for line in body.split("\n"):
        m = pingpong_pattern.match(line)

        if m is not None:
            if name is None: name = m.group('name')
            size = float(m.group('size'))
            if size not in data: data[size] = dict()
            s_sizes.add(size)
            groups = m.groupdict()
            for t_name in groups:
                if t_name.startswith("t_") == False: continue
                s_names.add(t_name)
                data[size][t_name] = float(groups[t_name])


    l_sizes = sorted(s_sizes)
    l_xtics = l_sizes[::int(ceil(len(l_sizes)/4.))]
    if l_sizes[-1] not in l_xtics: l_xtics.append(l_sizes[-1])
    xtics = (", ".join(['"%.7g" %.7f' % (x,x) for x in l_xtics]))
    l_names = sorted(s_names)

    plt.write("# ---v--- input: %d ---v--- \n" % dat_input)

    plot_name = "%s (%s)" % (name, filename)
    dat.write("### Input %d: %s" % (dat_input,plot_name))
    dat.write("\n# size")
    for t_name in l_names:
        dat.write("\t%s"%t_name)
    dat.write("\n")

    for size,values in sorted(data.items()):
        dat.write("%.7f" % size)
        for t_name in l_names:
            if t_name not in values:
                dat.write("\t-")
            else:
                dat.write("\t%.7f" % values[t_name])
        dat.write("\n")
    dat.write("\n\n")

    plt.write("set title '%s'\n" % plot_name.replace('_','\_'))
    plt.write("set xlabel 'Size [KB]'\n")
    plt.write("set ylabel 'Time [s]'\n")
    plt.write("set format y ' 10^{%T}'\n")
    plt.write("set autoscale y\n")
    plt.write("set xrange [%.7f:%.1f]\n" % (l_sizes[0]*0.9, l_sizes[-1]*1.1))
    plt.write("set xtics (%s)\n" % xtics)
    plt.write("#set key nobox at %.3f,%.3f\n" % (l_sizes[-1],0.01))
    plt.write("set output '%s_%d.png'\n\n" % (filename[:-4], dat_input))

    col=2
    plt.write("plot 'plot.dat' i %d u 1:%d t '%s' w lp lt %d lw 3 pt 7"%(
        dat_input,
        col, # column
        l_names[0].replace('_','\_'),
        col-1 # color
    ))
    col = col+1
    for t_name in l_names[1:]:
        plt.write(",\\\n     'plot.dat' i %d u 1:%d t '%s' w lp lt %d lw 3 pt 7"%(
            dat_input,
            col, # column
            t_name.replace('_','\_'), # nodes
            col-1 # color
        ))
        col=col+1
    plt.write("\n\n")
    plt.write("# ---^--- input: %d ---^--- \n" % dat_input)

    dat_input = dat_input + 1
    stderr.write(" done\n")



dat.close()
plt.write("""
pause -1""")
plt.close()
