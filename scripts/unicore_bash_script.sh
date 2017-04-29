#!/bin/bash -l
#SBATCH -J PCJ-tests
#SBATCH -N 2
#SBATCH --ntasks-per-node 48
#SBATCH --mem 129068
#SBATCH --time=0-03:30:00
#SBATCH -A GB65-15
#SBATCH --output="output_%j.out"
#SBATCH --error="output_%j.err"


timer=`date +%s`

function log {
  local output=$(echo "";echo "`date +'%y-%m-%d %H:%M:%S'`" ; while IFS= read -r line; do echo -e "\t$line"; done < <(echo -e "$@");echo "")
  echo "$output"
  echo "$output" 1>&2
}

function esc {
    echo "$@" | sed 's#\\#\\\\\\\\#g'
}
CWD=$(pwd)

log "Current date: $(esc "`date`")"
log "Master host: $(esc "`/bin/hostname`")"
log "Working directory: $(esc "`pwd`")"
log "Current script: $0\n$(esc "`cat -n $0`")"
log "Environment variables: $(esc "`env`")"
log "Set variables: $(esc "`set`"))"
log "CPU info: $(esc "`cat /proc/cpuinfo`")"
log "MEM info: $(esc "`cat /proc/meminfo`")"


# --- LOADING MODULES ---
log "Loading modules"
module load plgrid/tools/openmpi || exit 1
module load plgrid/tools/java8 || exit 1
# module load java
# module load cray-mpich

# --- SHOW VERSIONS ---
log "MPI version:\n * $(esc "`mpicc --showme:version 2>&1`")\n * $(esc "`mpicc --version 2>&1`")\n"
log "Java version:\n * $(esc "`java -version 2>&1`")"

# --- PREPARING WORKING DIR ---
# mkdir job-$SLURM_JOB_ID && cd job-$SLURM_JOB_ID

# --- COPYING AND COMPILING FOR MPI ---
#cp -r ../mpi/jgf/ray .
#cp -r ../mpi/jgf/ray_wtime .
#CC ray/raytracer.cpp -O3 -o ray.exe
#CC ray_wtime/raytracer.cpp -O3 -o ray_wtime.exe

# --- PREPARING NODES LIST ---
log "Preparing nodes list"
mpiexec hostname -s | sort > all_nodes.txt
mpiexec -npernode 1 hostname -s > all_nodes.uniq
# srun hostname -s | sort > all_nodes.txt
# uniq all_nodes.txt > all_nodes.uniq

log "All nodes: `uniq -c all_nodes.txt | xargs -I {} echo ' {}' | paste -sd ','`"

# --- RUNNING TESTS ---
log "Running tests"

PCJ_bin_jars=("PCJ-5.0.3.jar" )
PCJ_tests_jars=("PCJ5-tests.jar")

CORES_ON_NODE=(64 32 16 12 8 4 2 1)
BENCHMARK_NAMES=(Barrier Broadcast PiInt PiMC RayTracerC RayTracerD MolDynC MolDynD)

nodes=$( cat all_nodes.uniq | wc -l )
while [ $nodes -gt 0 ]; do
    for threads in "${CORES_ON_NODE[@]}"; do
#        srun -N $nodes -n $nodes hostname -s | sort > nodes.txt
        head -$nodes all_nodes.uniq > nodes.txt

        cat nodes.txt > nodes.now
        for i in `seq 2 $threads`; do
            cat nodes.txt >> nodes.now
        done
        sort nodes.now -o nodes.txt
        uniq nodes.txt > nodes.uniq
        log "Using ${nodes}n${threads}t: `uniq -c nodes.txt | xargs -I {} echo ' {}' | paste -sd ','`"

#        for benchmark in ray ray_wtime; do
#            for size in C D; do
#                log "Starting $benchmark$size on $nodes nodes each $threads threads using MPI"
#                filename="`echo $benchmark$size | tr '[:upper:]' '[:lower:]'`.out"
#                echo -e "$benchmark using:\t$nodes nodes\t$threads threads" >> $filename
#
#                srun -N $nodes -n $(( $nodes * $threads )) -c $(( 48 / $threads )) -w ./nodes.txt ./${benchmark}.exe $size | tee -a $filename >(tee >&2)
#            done
#        done


        for benchmark in "${BENCHMARK_NAMES[@]}"; do
            if [ $benchmark = "PingPong" ] &&
                    [ $nodes -ne 2 -o $threads -ne 1 ] &&
                    [ $nodes -ne 1 -o $threads -ne 2 ]; then
                continue;
            fi

            for idx in "${!PCJ_bin_jars[@]}"; do
                PCJ_bin_jar=${PCJ_bin_jars[$idx]}
                PCJ_tests_jar=${PCJ_tests_jars[$idx]}

                log "Starting $benchmark on $nodes nodes each $threads threads using ${PCJ_bin_jar} jar file"
                filename="`echo ${PCJ_bin_jar}_${benchmark} | tr '[:upper:]' '[:lower:]'`.out"
                if [ $benchmark = "PingPong" ]; then
                    filename="`echo ${PCJ_bin_jar}_${benchmark}_${nodes}n_${threads}t | tr '[:upper:]' '[:lower:]'`.out"
                fi

                echo -e "$benchmark using:\t$nodes nodes\t$threads threads" >> $filename

#                srun -N $nodes -n $nodes -c 48 -w ./nodes.uniq bash -c "java \
                mpiexec --hostfile nodes.uniq bash -c "java \
                        -Xmx120g \
                        -cp ..:../${PCJ_bin_jar}:../${PCJ_tests_jar} \
                        -Dpcj.chunksize=8192 \
                        org.pcj.tests.Main \
                        $benchmark \
                        nodes.txt" | tee -a $filename >(tee >&2)
            done
        done
    done

    nodes=$(( $nodes / 2 ))
done

# --- PROCESS RESULT ---
log "Processing results"

python $CWD/process_results.py

# --- COMPLETED ---
timer=$(( `date +%s` - $timer ))
h=$(( $timer / (60 * 60) ))
m=$(( ($timer / 60) % 60 ))
s=$(( $timer % 60 ))
log "Script completed after ${h}h ${m}m ${s}s."

# EOF

