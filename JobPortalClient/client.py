#! /usr/bin//python3


import argparse
import concurrent.futures
import datetime
import math
import os
import json
import random
import requests
import string
import time

WEB_SERVER_URL = 'http://localhost:8080/job-portal/profiles'

DEGREES = ['BA','BS','BBA','BEng','BFA','MA','MS','MEng','MBA','PhD','MD']

UNIVERSITIES = ['CSUEB','SJSU','SCU','CMU','UFL','SUNYB','CSU']

SKILLS = ['HTML','Python','Java','SQL','C++','JavaScript','Selenium','TypeScript','NodeJS','Ruby','AWS']

WRITE_THREAD_POOL_SIZE = 2

def random_string(length):
    return ''.join(random.choices(string.ascii_letters, k=length))
    
def random_string_with_numbers(length):
    return ''.join(random.choices(string.ascii_letters + string.digits + '       ', k=length))

def random_number_string(length):
    return ''.join(random.choices(string.digits, k=length))

def random_degree():
    return ''.join(random.choices(DEGREES))

def random_university():
    return ''.join(random.choices(UNIVERSITIES))

def random_skills_list():
    return random.choices(SKILLS, k=random.randint(1,5))

def random_date():
    year = random.randint(1970, 2022)
    month = random.randint(1, 12)
    day = random.randint(1, 31 if month in [1, 3, 5, 7, 8, 10, 12] else 30 if month in [4, 6, 9, 11] else 28)
    return datetime.date(year, month, day).strftime('%m-%d-%Y')

def generate_profile():
    return {
            'fullName': random_string(50),
            'dob': random_date(),
            'address': random_string_with_numbers(45),
            'phone': random_number_string(10),
            'degree': random_degree(),
            'university': random_university(),
            'yoe': random.randint(0, 20),
            'skills': random_skills_list()
    }

def get_profiles(paramName, paramValue):
    url = WEB_SERVER_URL
    params = {paramName: paramValue}
    response = requests.get(url, params=params)
    if response.status_code != 200:
        raise Exception(response.text)
    return response.json();

def save_profile(profile):
    url = WEB_SERVER_URL
    headers = {'Content-Type': 'application/json'}
    response = requests.post(url, headers=headers, data=profile)
    return response.status_code, response.text

def generator(n, fileName):
    print("Running generator function for {} profiles, file: {}")
    with open(fileName, 'w') as f:
        for i in range(n):
            p = generate_profile()
            f.write(json.dumps(p) + '\n')

def reader(fieldKey, fieldValue, repeat=0):
    print("Running reader function for ", fieldKey, fieldValue, repeat)
    if not repeat:
        profiles = get_profiles(fieldKey, fieldValue)
        print("Read {} profiles".format(len(profiles)))
        return

    times = []
    for i in range(repeat):
        start_time = time.time()
        get_profiles(fieldKey, fieldValue)
        times.append(time.time() - start_time)

    print("Average time taken for {} requests is: {} ms".format(repeat, round(1000*sum(times)/len(times), 3)))


def is_json(mystr):
    try:
        json.loads(mystr)
    except ValueError:
        return False
    return True

def writer(profile, fileName):
    print("Running writer function")
    
    if profile:
        if not is_json(profile):
            raise ValueError('Profile input is not a valid JSON');
        print(save_profile(profile))
        return

    if fileName:
        if not os.path.exists(fileName):
            raise ValueError("File {} does not exists".format(fileName))
        saved = 0
        start_time = time.time()
        with open(fileName, 'r') as f:
            if WRITE_THREAD_POOL_SIZE == 1:
                for line in f:
                    code, msg = save_profile(line)
                    if code != 200:
                        raise Exception("Saved {} profiles, encountered error: {}".format(saved, msg))
                    saved += 1
            else:
                with concurrent.futures.ThreadPoolExecutor(max_workers=WRITE_THREAD_POOL_SIZE) as executor:
                    futures = []
                    for line in f:  
                        futures.append(executor.submit(save_profile, line))
                    for future in concurrent.futures.as_completed(futures):
                        result = future.result()
                        code, msg = result
                        if code != 200:
                            raise Exception("Saved {} profiles, encountered error: {}".format(saved, msg))
                        saved += 1
        print("Time taken to write {} profiles is {}s".format(saved, round(time.time()-start_time, 3)))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-g", "--generator", help="Run the generator function", action="store_true")
    parser.add_argument("-n", "--count", help="Number of profiles to save", type=int)
    parser.add_argument("-r", "--reader", help="Run the reader function", action="store_true")
    parser.add_argument("-k", "--fieldKey", help="Name of the field for read query", type=str)
    parser.add_argument("-v", "--fieldValue", help="Value of the field for read query", type=str)
    parser.add_argument("-b", "--benchmark", help="Number of read requests to run for benchmarking", type=int)
    parser.add_argument("-w", "--writer", help="Run the writer function to write a single profile", action="store_true")
    parser.add_argument("-p", "--profile", help="A job portal profile to save, given as JSON string", type=str)
    parser.add_argument("-f", "--file", help="File name to generate profiles into, or read profiles from", type=str)
    args = parser.parse_args()

    if args.generator:
        if not args.count or not args.file:    
            raise ValueError("Invalid input, must provide both count and file for generator option")
        generator(args.count, args.file)
    if args.reader:
        if not args.fieldKey or not args.fieldValue:
            raise ValueError("Invalid input, must provide both field key and value for reader option")
        reader(args.fieldKey, args.fieldValue, args.benchmark)
    if args.writer:
        if not args.profile and not args.file:
            raise ValueError("Invalid input, must provide a profile JSON to write or file to read profiles for writer option")
        writer(args.profile, args.file)




