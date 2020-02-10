#!/usr/bin/env python
# coding: utf-8

# In[1]:


import numpy as np
import json
from config import parse_dataset_output, mongo_uri, mongo_db


# In[2]:


with open(parse_dataset_output) as json_file:
    movies = json.load(json_file)
    


# In[3]:


print(movies[0].keys())


# In[5]:


# directors
all_directors = {}
for movie in movies:
    directors = []
    if movie['directors'] is None:
        movie['directors'] = []
        continue
    for d in movie['directors']:
        if d[0] is None:
            continue
        director = {'id': d[0], 'name': d[1]}
        directors.append(director)
        all_directors[d[0]] = d[1]
    movie['directors'] = directors
director_list = [{"_id": did, "name": all_directors[did]} for did in all_directors]


# In[6]:


# characters
all_actors = {}
for movie in movies:
    characters = []
    if movie['characters'] is None:
        movie['characters'] = []
        continue
    for c in movie['characters']:
        if c[2] is None :
            continue
        character = {'actor_id': c[2], 'actor_name': c[1], 'name': c[0]}
        characters.append(character)
        all_actors[c[2]] = c[1]
    movie['characters'] = characters
actor_list = [{"_id": aid, "name": all_actors[aid]} for aid in all_actors]


# In[7]:


all_genres = set()
# genres
for movie in movies:
    if movie['genres'] is None:
        movie['genres'] = []
        continue
    genres = movie['genres'].split(',')
    movie['genres'] = genres
    for genre in genres:
        all_genres.add(genre)
genre_list = list(all_genres)


# In[8]:


all_years = set()
# years
for movie in movies:
    if movie['year'] is None:
        continue
    year = int(movie['year'])
    movie['year'] = year
    all_years.add(year)
year_list = list(all_years)


# In[9]:


# renamings
for movie in movies:
    if "tid" in movie:
        movie["_id"] = movie["tid"]
        del movie["tid"]
    
    if "originaltitle" in movie:
        movie["original_title"] = movie["originaltitle"]
        del movie["originaltitle"]


# In[12]:


# fix missing titles
for movie in movies:
    if movie["title"] is None:
        if movie["original_title"] is None:
            print(movie["_id"])
        movie["title"] = movie["original_title"]


# In[13]:


from pymongo import MongoClient
from pymongo import UpdateOne

client = MongoClient(mongo_uri)
db = client[mongo_db]


# In[14]:


requests = []
for movie in movies:
    requests.append(UpdateOne({"_id": movie["_id"]}, {"$set": movie}, upsert=True))

try:
    db.movies.bulk_write(requests, ordered=False)
except BulkWriteError as bwe:
    print(bwe.details)


# In[23]:


requests = []
for actor in actor_list:
    requests.append(UpdateOne({"_id": actor["_id"]}, {"$set": actor}, upsert=True))
    
try:
    db.actors.bulk_write(requests, ordered=False)
except BulkWriteError as bwe:
    print(bwe.details)   


# In[24]:


requests = []
for director in director_list:
    requests.append(UpdateOne({"_id": director["_id"]}, {"$set": director}, upsert=True))

try:
    db.directors.bulk_write(requests, ordered=False)
except BulkWriteError as bwe:
    print(bwe.details)


# In[ ]:




