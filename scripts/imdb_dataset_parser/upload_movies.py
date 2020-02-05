#!/usr/bin/env python
# coding: utf-8

# In[42]:


import numpy as np
import json


# In[43]:


with open('movies.json') as json_file:
    movies = json.load(json_file)
    


# In[44]:


print(movies[0].keys())


# In[45]:


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


# In[46]:


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


# In[47]:


# genres
for movie in movies:
    characters = []
    if movie['genres'] is None:
        movie['genres'] = []
        continue
    movie['genres'] = movie['genres'].split(',')


# In[48]:


# renamings
for movie in movies:
    if "tid" in movie:
        movie["_id"] = movie["tid"]
        del movie["tid"]
    
    if "originaltitle" in movie:
        movie["original_title"] = movie["originaltitle"]
        del movie["originaltitle"]


# In[49]:


movies[37276]


# In[53]:


from pymongo import MongoClient
from pymongo import UpdateOne

client = MongoClient()
db = client['task2-test']


# In[54]:


db.movies.find_one()


# In[56]:


requests = []
for movie in movies:
    requests.append(UpdateOne({"_id": movie["_id"]}, {"$set": movie}, upsert=True))

try:
    db.movies.bulk_write(requests, ordered=False)
except BulkWriteError as bwe:
    print(bwe.details)


# In[57]:


requests = []
for actor in actor_list:
    requests.append(UpdateOne({"_id": actor["_id"]}, {"$set": actor}, upsert=True))
    
try:
    db.actors.bulk_write(requests, ordered=False)
except BulkWriteError as bwe:
    print(bwe.details)   


# In[58]:


requests = []
for director in director_list:
    requests.append(UpdateOne({"_id": director["_id"]}, {"$set": director}, upsert=True))

try:
    db.directors.bulk_write(requests, ordered=False)
except BulkWriteError as bwe:
    print(bwe.details)


# In[ ]:




