#!/bin/bash -l
# #SBATCH -J PCJ-tests
# #SBATCH -N 1
# #SBATCH -n 28
# #SBATCH --mem 18000
# #SBATCH --time=24:00:00
# #SBATCH --output="PCJ-tests_%j.out"
# #SBATCH --error="PCJ-tests_%j.err"

timer=`date +%s`

function log {
  output=$(echo "";echo "`date +'%y-%m-%d %H:%M:%S'`" ; while read -r line; do echo -e "\t$line"; done < <(echo -e "$@");echo "")
  echo "$output"
  echo "$output" 1>&2
}

log "Current date: `date`"
log "Master host: `/bin/hostname`"
log "Working directory: `pwd`"
log "Environment variables: `env`"
log "Set variables: `set`"
log "CPU info: `cat /proc/cpuinfo`"
log "MEM info: `cat /proc/meminfo`"


# --- LOADING MODULES ---
log "Loading modules"
module load plgrid/tools/openmpi || exit 1
module load plgrid/tools/java8 || exit 1


# --- SHOW VERSIONS ---
log "Tools:\n * `mpicc --showme:version 2>&1`\n * `mpicc --version 2>&1`\n * `java -version 2>&1`"


# --- PREPARING NODES LIST ---
log "Preparing nodes list"
mpiexec hostname -s | sort > all_nodes.txt
mpiexec -npernode 1 hostname -s > all_nodes.uniq

log "All nodes: `uniq -c all_nodes.txt | xargs -I {} echo ' {}' | paste -sd ','`"

# --- RUNNING TESTS ---.
log "Running tests"

CORES_ON_NODE=(64 32 16 12 8 4 2 1)
BENCHMARK_NAMES=(Barrier Broadcast PiInt PiMC RayTracerC RayTracerD MolDynC MolDynD)
nodes=$( cat all_nodes.uniq | wc -l )
while [ $nodes -gt 0 ]; do
    for threads in "${CORES_ON_NODE[@]}"; do
        head -$nodes all_nodes.uniq > nodes.txt
        for i in `seq 2 $threads`; do
            head -$nodes all_nodes.uniq >> nodes.txt
        done
        sort nodes.txt -o nodes.txt
        uniq nodes.txt > nodes.uniq
        log "Using ${nodes}n${threads}t: `uniq -c nodes.txt | xargs -I {} echo ' {}' | paste -sd ','`"

        for benchmark in "${BENCHMARK_NAMES[@]}"; do
            log "Starting $benchmark on $nodes nodes each $threads threads"
            filename="`echo $benchmark | tr '[:upper:]' '[:lower:]'`.out"
            echo -e "$benchmark using:\t$nodes nodes\t$threads threads" >> $filename

            mpiexec --hostfile nodes.uniq bash -c "java -Xmx16g -cp .:PCJ-ant.jar:PCJ-tests.jar org.pcj.tests.Main $benchmark nodes.txt" | tee -a $filename
        done
    done
    nodes=$(( $nodes / 2 ))
done

# ... pingpong ...
log "PingPong on 2 nodes"
head -2 all_nodes.uniq > nodes.uniq
mpiexec --hostfile nodes.uniq bash -c "java -Xmx16g -cp .:PCJ-ant.jar:PCJ-tests.jar org.pcj.tests.Main PingPong nodes.uniq" | tee -a pingpong_2n1t.out

log "PingPong on 1 node"
head -1 all_nodes.uniq > nodes.uniq
cat nodes.uniq nodes.uniq > nodes.txt
mpiexec --hostfile nodes.uniq bash -c "java -Xmx16g -cp .:PCJ-ant.jar:PCJ-tests.jar org.pcj.tests.Main PingPong nodes.txt" | tee -a pingpong_1n2t.out

# --- PROCESS RESULT ---
log "Processing results"

python process_results.py

# --- COMPLETED ---
timer=$(( `date +%s` - $timer ))
h=$(( $timer / (60 * 60) ))
m=$(( ($timer / 60) % 60 ))
s=$(( $timer % 60 ))
log "Script completed after ${h}h ${m}m ${s}s."

# EOF
