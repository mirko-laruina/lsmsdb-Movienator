#!/usr/bin/env python
# coding: utf-8

import json
from multiprocessing.pool import ThreadPool
from py2neo import Graph, Node, Relationship
from pymongo import MongoClient

import config

# configuration
keep_movie_attrs = ['_id', 'title', 'year', 'poster']
keep_user_attrs  = ['_id', 'username']
N_THREADS=4

def sync_movie(m):
    movie = {k:v for k,v in m.items() if k in keep_movie_attrs}
    n = Node("Movie", **movie)
    graph.merge(n, "Movie", "_id")

def sync_user(u):
    u['_id'] = u['_id'].__str__()
    user = {k:v for k,v in u.items() if k in keep_user_attrs}
    m = Node("User", **user)
    graph.merge(m, "User", "_id")

def sync_rating(r):
    RATED = Relationship.type("RATED")

    u = Node("User", _id=r["_id"]["user_id"].__str__())
    u.__primarylabel__ = "User"
    u.__primarykey__ = "_id"
    m = Node("Movie", _id=r["_id"]["movie_id"])
    m.__primarylabel__ = "Movie"  
    m.__primarykey__ = "_id"
    graph.merge(RATED(u,m, rating=r['rating'], date=r['date']))

if __name__ == "__main__":
    # set-up clients
    # Mongo
    client = MongoClient(config.mongo_uri)
    db = client[config.mongo_db]

    # connect to graph
    graph = Graph(uri=config.neo4j_uri, auth=config.neo4j_auth)

    # init thread pool
    pool = ThreadPool(N_THREADS)

    # synchronize movies
    movies = db.movies.find()
    n_movies = db.movies.count_documents({})
    movie_pool_res = pool.imap_unordered(sync_movie, movies)

    print("Movies:  %6.2f%%" % (0), end="")
    for i, _ in enumerate(movie_pool_res):
        print("\rMovies:  %6.2f%%" % ((i+1)/n_movies*100), end="")
    print("\rMovies:  %6.2f%%" % (100))

    # synchronize users
    users = db.users.find()
    n_users = db.users.count_documents({})
    user_pool_res = pool.imap_unordered(sync_user, users)

    print("Users:   %6.2f%%" % (0), end="")
    for i, _ in enumerate(user_pool_res):
        print("\rUsers:   %6.2f%%" % ((i+1)/n_users*100), end="")
    print("\rUsers:   %6.2f%%" % (100))

    # synchronize ratings
    ratings = db.ratings.find()
    n_ratings = db.ratings.count_documents({})
    rating_pool_res = pool.imap_unordered(sync_rating, ratings)
    
    print("Ratings: %6.2f%%" % (0), end="")
    for i, _ in enumerate(rating_pool_res):
        print("\rRatings: %6.2f%%" % ((i+1)/n_ratings*100), end="")
    print("\rRatings: %6.2f%%" % (100))

    # follow relationships are only on Neo4j
