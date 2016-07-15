#!/bin/bash
cat <<EOT >$1
#!/bin/bash
#SBATCH -t 6:00:00
#SBATCH -p batch
#SBATCH -N 1
#SBATCH -n 1
#SBATCH -o $2
#SBATCH -e $3
#SBATCH --mem-per-cpu=4800
java -Xmx4800m -cp $4 org.renci.databridge.engines.batch.BatchWorker $5
EOT
