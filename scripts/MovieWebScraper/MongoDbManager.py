# -*- coding: utf-8 -*-
"""
Created on Thu Feb  6 16:13:41 2020

@author: Gianmarco Petrelli
"""

import MovieScraper as ms
from pymongo import MongoClient, UpdateOne
from pprint import pprint
from config import mongo_uri, mongo_db

from datetime import datetime

class MongoManager:
    
    server_ip = "127.0.0.0"
    
    def set_ip(ip):
        MongoManager.server_ip = ip
        
    def __init__(self):
        
        self.client = MongoClient(mongo_uri)
        self.db = self.client.admin
        self.PrintDBs()
        #print(self.db)
        
    def PrintDBs(self):
        print(self.client.list_database_names())
        
    def PrintCollectsInDb(self):
        print(self.db.list_collection_names())
    
    def PrintCollection(self,coll):
        for doc in coll:
            pprint(doc)
        
    def ServerStatus(self):
        # Issue the serverStatus command and print the results
        serverStatusResult=self.db.command("serverStatus")
        pprint(serverStatusResult)
        
    def SetDBName(self,DBname:str):
        self.db = self.client[DBname]
        print("db selezionato : "+str(DBname))
        
        
    def getCollection(self,CollName,limn=0,skipN=0):
        if limn == 0:
            return self.db[CollName].find().skip(skipN)#returns a cursor
        else:
            return self.db[CollName].find().skip(skipN).limit(limn)#returns a cursor
    
    def getMoviesByLastScraped(self, limn=0):
        queryset = self.db["movies"].find().sort([('last_scraped', 1)])
        if limn == 0:
            return queryset
        else:
            return queryset.limit(limn)
    
    """"""""""""""
    
    def UpdateMongoMovies(self,coll_name,nrows=20):#make use of indxes file
        
        mysource_p = ms.web_sources.get("mymovies",None)
        if mysource_p == None :
            raise Exception("Sorry, requested source not available")
        
        # #recupero l'indice
        # try:
        #     f=open("UpdateIndex.txt", "r")
        #     skipIdx=int(f.read())
        # except OSError as oe:
        #     skipIdx = 0
        
        # coll_iterator = self.getCollection(coll_name,nrows,skipIdx)####
        coll_iterator = self.getMoviesByLastScraped(nrows)
        
        for movie in coll_iterator:
            print("\n--getting movie--\n")
            print(movie["_id"], movie["title"], movie["title_ita"])

            # mark movie as scraped to prevent other scrapers to scrape the same
            # movie
            movie_in_db = self.db['movies'].find_one_and_update({
                '_id': movie['_id']
            }, {
                '$set': {
                    'last_scraped': datetime.now()
                }
            })

            # check if copy in db changed since last fetch
            if movie_in_db['last_scraped'] > movie['last_scraped']:
                # another scraper took it
                print("\n--already scraped--\n")
                continue

            
            #extract movie by source
            #data from mymovies
            try:
                scrape = ms.MovieScraper()
                mm_movie_info = scrape.getMovieFromMyMovie(movie)
            except Exception as e:
                print(e)
                mm_movie_info = None
            #data from imdb
            try:
                """aggiungere dati di imdb per film con stesso id"""
                im_movie_info = scrape.LoadMovie(movie["_id"])
            except Exception as e:
                print(e)
                im_movie_info = None

            pprint(mm_movie_info)
            
            #updatedinfo
            upd_dic ={'ratings':[]}
                    
            #mymovie data manager
            if (mm_movie_info == None):
                print("\nnessun contenuto mymovie da aggiornare\n")
            else:
                print(mm_movie_info)
                if ("ratings" in movie.keys()):
                    #append new rate
                    mm_movie_aggr_info = mm_movie_info["movie"]["aggregateRating"]
                    if (mm_movie_aggr_info != None):
                        if (mm_movie_aggr_info["ratingValue"] != ''):
                            ratepoints = float(mm_movie_aggr_info["ratingValue"])
                            newrate = {
                                "source": "MyMovies",
                                "avgrating": ratepoints,
                                "count": mm_movie_aggr_info["ratingCount"], 
                                "weight": 1,
                                "last_update": datetime.now()
                            }
                            upd_dic["ratings"].append(newrate)
                
                if "description" in mm_movie_info:
                    upd_dic["description"] = mm_movie_info["description"]
                    upd_dic["description_ita"] = mm_movie_info["description"]
                
                if "image" in mm_movie_info:
                    upd_dic["poster"] = mm_movie_info["image"][0]["url"]
                
            #imdb data manager
            if (im_movie_info ==None):
                print("\nnessun contenuto imdb da aggiornare\n")
            else:
                #append new rate
                im_movie_aggr_info = im_movie_info["movie"]["aggregateRating"]
                if (im_movie_aggr_info !=None):
                    if (im_movie_aggr_info["ratingValue"] != ''):
                        ratepoints =float(im_movie_aggr_info["ratingValue"])
                        newrate = {
                            "source":"IMDb",
                            "avgrating": ratepoints,
                            "count": im_movie_aggr_info["ratingCount"],
                            "weight": 0.5,
                            "last_update": datetime.now()
                        }
                        upd_dic["ratings"].append(newrate)
                
                #update diffs on mongodb
                im_movie_info = im_movie_info["movie"]
                
                # IMDb overrides mymovies result
                if "description" in im_movie_info and im_movie_info['description']:
                    upd_dic["description"] = im_movie_info["description"]

                if "image" in im_movie_info and im_movie_info['image']:
                    upd_dic["poster"] = im_movie_info["image"]
                        
                for attribute in ms.MovieScraper.EXTRA_ATTRIBUTES:    
                    if im_movie_info[attribute]:
                        upd_dic[attribute] = im_movie_info[attribute]
            
            pprint(upd_dic)
            
            #load updated movie ratings
            operations = []
            for rating in upd_dic['ratings']:
                operations += [
                    UpdateOne(
                        {'_id': movie['_id']},
                        {'$pull': {
                            "ratings": {
                                "source": rating['source']
                            }
                        }}
                    ),
                    UpdateOne(
                        {'_id': movie['_id']},
                        {'$push': {
                            "ratings": rating
                        }}
                    ),
                ]

            # merge ratings
            ratings = {}
            for r in movie_in_db['ratings']:
                if r['source'] == 'internal':
                    if r['count'] > 0:
                        r['avgrating'] = r['sum'] / r['count']
                        ratings['internal'] = r
                else:
                    ratings[r['source']] = r
            for r in upd_dic['ratings']:
                ratings[r['source']] = r # override existing ratings

            pprint(ratings)

            # calculate new total_rating
            rating_sum = sum([r['avgrating']*r['weight'] for s,r in ratings.items()])
            denominator = len(ratings)
            if denominator != 0:
                upd_dic['total_rating'] = rating_sum/denominator
                pprint(upd_dic['total_rating'])

            # update other info
            del upd_dic['ratings'] # remove ratings since already updated
            upd_dic['last_scraped'] = datetime.now()
            operations.append(UpdateOne({
                    '_id': movie['_id']
                }, {
                    '$set': upd_dic
            }))

            if operations:
                self.db['movies'].bulk_write(operations)
            
            print("\n---movie updated---")

         
if __name__ == "__main__":#test
    """INIT"""
    a = MongoManager()
    a.SetDBName(mongo_db)
    a.PrintCollectsInDb()
    """"""
    
    a.UpdateMongoMovies('movies',1)#AGGIORNA K MOVIES (k=1)
    """stampa k film (k=1)"""
    """
    c1 = a.getCollection('movies',2)
    a.PrintCollection(c1)
    """
    #a.ServerStatus()
