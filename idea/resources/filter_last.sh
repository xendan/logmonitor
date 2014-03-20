if [ -z "$date" ] || ! grep -q '$date' $log_path; then
    cp $log_path  $download_path
else
    sed "0,/$date/d" <$log_path >  $download_path
fi
du -hb  $download_path |awk '{print $1}'