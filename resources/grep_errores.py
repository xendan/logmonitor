import sys
from os.path import expanduser
new_f = open(expanduser("~") + '/.logmonitor/last.log', 'w')
for line in open(sys.argv[1],'r').readlines():
    if sys.argv[2] in line:
        new_f.write(line)
