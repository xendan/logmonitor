lines=()

rm $download_path
touch $download_path

function shift_lines {
        for i in `seq 2  $n_of_lines`;
        do
                ((idx=i-1))
                lines[((i-2))]=${lines[idx]}
        done
}

function printl {
        for i in `seq 1 $n_of_lines`;
        do
                echo "${lines[((i-1))]}" >> $download_path
        done
}

for i in `seq 1 $n_of_lines`;
do
        lines[((i-1))]=''
done

found_indx=0
while read line; do
        echo $line
        ((idx=$n_of_lines-1))
        if [[ $line =~ "^$pattern.*" ]]; then
                shift_lines
                lines[$idx]=$line
                if (( found_indx > 0)); then
                        ((found_indx++))
                fi
        else
                lines[$idx]+="
$line"
        fi
        if [[ $line == "$date.*" ]]; then
                found_indx=1
        fi
        if ((found_indx>(($n_of_lines/2)))); then
                printl
                exit 0
        fi
done < $log_path
echo "HELAS, nothing was found!!!"
for i in `seq 1 $n_of_lines`;
do
    echo "${lines[((i-1))]}"
done
