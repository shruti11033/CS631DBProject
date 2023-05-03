# Job Portal Client Usage

## Generating random profiles dataset

```py
# This command Generates 200,00 profiles and wtites it into a file
# Each line of the file is JSON string of profile
python3 client.py --generator --count 200000 --file profiles200k.txt
```

## Saving generated profiles dataset in DB

```py
# This command reads each line of file as a profile and sends it to the web-server as a request to write
# The web-server saves each profile as a row in the database
python3 client.py --writer --file profiles200k.txt
```

## Fetching profiles using query filter and Read performance bechmarking

```py
# Fetching rows where degree = 'BS' from database, running the read 20 times, to average the fetch time
# Selected rows are converted back to a profile JSON and sent to this client
# This command emits the average time taken to served such a request end-to-end
python3 client.py --reader --fieldKey degree --fieldValue BS --benchmark 20
```