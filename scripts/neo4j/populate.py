import json
import requests
import random
import sys

users_to_create = 100
starting_id = 0

base_url = "http://45.76.92.122:8080/api/v1/"
sids = []

if len(sys.argv) <= 1 or (sys.argv[1] != 'r' and sys.argv[1] != 'f'):
    print("Run with 'r' to rate random movies, 'f' to follow random users")
    sys.exit(1)

print("Creating user from test_user_"+str(starting_id)+" to test_user_"+str(starting_id+users_to_create-1));
for i in range(starting_id, starting_id+users_to_create):
    user = 'test_user_'+str(i)
    #print("Creating user ", user)
    r = requests.post(base_url+"auth/register", data={
        'username': user,
        'password': user,
        'email': user
    })
    if r.json()['success']:
        sids.append(r.json()['response']['sessionId'])
    else:
        r = requests.post(base_url+"auth/login", data={
            'username': user,
            'password': user,
        })
        sids.append(r.json()['response']['sessionId'])


if sys.argv[1] == "r":
    ratings_per_user = 50
    print("Reading movies id from file")
    movies = []
    with open("random_movies", "r") as f:
        movies = [a.strip() for a in f.readlines()]

    print("Rating", ratings_per_user, "random movies per user")
    for sid in sids:
        for i in range(0, ratings_per_user):
            r = requests.put(base_url+"/movie/"+random.choice(movies)+"/rating", data={
                'sessionId': sid,
                'rating': random.randint(1, 5)
            })
        print("Created ratings using", sid)

elif sys.argv[1] == "f":
    print("Following random test_user")
    follows_per_user = 10

    for sid in sids:
        for i in range(0, follows_per_user):
            while True:
                rand_n = str(random.randint(starting_id, starting_id+users_to_create-1))
                r = requests.post(base_url+"/user/test_user_"+rand_n+"/follow", data={
                    'sessionId': sid
                })
                if r.json()['success']:
                    break
        print("Following users with", sid)


