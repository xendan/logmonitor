if grep -q '$date' $log_path; then
sed "0,/$date/d" <$log_path >  $download_path
 else cp $log_path  $download_path
 fi