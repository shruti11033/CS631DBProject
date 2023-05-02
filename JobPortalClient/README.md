# Job Portal Client Usage

## Generating random profiles

```py
python3 client.py --generator --count 200000 --file profiles200000.txt
```

## Saving generated profiles

```py
python3 client.py --writer --file profiles200000.txt
```

## Fetching profiles

```py
python3 client.py --reader --fieldKey degree --fieldValue BS --benchmark 10
```