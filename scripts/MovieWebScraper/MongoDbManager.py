# -*- coding: utf-8 -*-
"""
Created on Thu Feb  6 16:13:41 2020

@author: Gianmarco Petrelli
"""

import MovieScraper as ms
from pymongo import MongoClient
from pprint import pprint

class MongoManager:
    
    server_ip = "127.0.0.0"
    
    def set_ip(ip):
        MongoManager.server_ip = ip
        
    def __init__(self):
        
        self.client = MongoClient("54.242.141.176",27017)
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
    
    """"""""""""""
    
    def UpdateMongoMovies(self,coll_name,nrows=20):#make use of indxes file
        
        mysource_p = ms.web_sources.get("mymovies",None)
        if mysource_p == None :
            raise Exception("Sorry, requested source not available")
        
        #recupero l'indice
        try:
            f=open("UpdateIndex.txt", "r")
            skipIdx=int(f.read())
        except OSError as oe:
            skipIdx = 0
        
        coll_iterator = self.getCollection(coll_name,nrows,skipIdx)####
        
        for movie in coll_iterator:
            #extract movie by source
            #data from mymovies
            scrape = ms.MovieScraper()
            mm_movie_info = scrape.getMovieFromMyMovie(movie)
            #data from imdb
            """aggiungere dati di imdb per film con stesso id"""
            im_movie_info = scrape.LoadMovie(movie["_id"])
            
            #updatedinfo
            upd_dic ={}
            
            #mymovie data manager
            if (mm_movie_info == None):
                print("\nnessun contenuto mymovie da aggiornare\n")
            else:
                
                if ("ratings" in movie.keys()):
                    yetthere = False
                    #if the movie info already update skip movie
                    for rate in movie["ratings"]:
                        if rate["source"]=="MyMovies":
                            yetthere = True
                    if not yetthere:
                        upd_dic["ratings"] = []
                        #append new rate
                        mm_movie_aggr_info = mm_movie_info["movie"]["aggregateRating"]
                        if (mm_movie_aggr_info != None):
                            ratepoints =float(mm_movie_aggr_info["ratingValue"])*2
                            newrate = {"source":"MyMovies","avgrating":ratepoints,"count":mm_movie_aggr_info["ratingCount"],"weight":1}
                            upd_dic["ratings"].append(newrate)
                        #load other rates
                        for rate in movie["ratings"]:
                                upd_dic["ratings"].append(rate)
                 
                   
                if ("ratings" not in upd_dic.keys()):
                    upd_dic["ratings"] = []
                    
                
                #update diffs on mongodb
                mm_movie_info = mm_movie_info["movie"]
                upd_dic["date"]= mm_movie_info["datePublished"]
                upd_dic["genres"] = []
                upd_dic["genres"].append(str(mm_movie_info["genre"]))
                upd_dic["description"] = mm_movie_info["description"]
                upd_dic["poster"] = mm_movie_info["image"][0]["url"]
                
            #imdb data manager
            if (im_movie_info ==None):
                print("\nnessun contenuto imdb da aggiornare\n")
            else:
                
                if "ratings" not in upd_dic.keys():
                    upd_dic["ratings"] = []
                
                if ("ratings" in movie.keys()):
                    yetthere = False
                    #if the movie info already update skip movie
                    for rate in movie["ratings"]:
                        if rate["source"]=="IMDb":
                            yetthere = True
                    if not yetthere:
                        #append new rate
                        im_movie_aggr_info = im_movie_info["movie"]["aggregateRating"]
                        if (im_movie_aggr_info !=None):
                            ratepoints =float(im_movie_aggr_info["ratingValue"])
                            newrate = {"source":"IMDb","avgrating":ratepoints,"count":im_movie_aggr_info["ratingCount"],"weight":1}
                            upd_dic["ratings"].append(newrate)
                    #load other rates
                    for rate in movie["ratings"]:
                        upd_dic["ratings"].append(rate)
                
                #update diffs on mongodb
                im_movie_info = im_movie_info["movie"]
                if ("genres" not in upd_dic.keys()):
                    upd_dic["genres"] = []
                    if str(type(im_movie_info["genre"])) == "<class 'list'>":
                        for gen in im_movie_info["genre"]:
                            upd_dic["genres"].append(str(gen))
                    else:
                        upd_dic["genres"].append(str(im_movie_info["genre"]))
                else:
                    if im_movie_info["genre"] not in upd_dic["genres"]:
                        if str(type(im_movie_info["genre"])) == "<class 'list'>":
                            for gen in im_movie_info["genre"]:
                                upd_dic["genres"].append(str(gen))
                        else:
                            upd_dic["genres"].append(str(im_movie_info["genre"]))
                
                if ("date" not in upd_dic.keys()):
                    upd_dic["date"] = im_movie_info["datePublished"]
                else:
                    if (upd_dic["date"]==""):
                        upd_dic["datePublished"] = im_movie_info["datePublished"]
                
                
                if ("description" not in upd_dic.keys()):
                    upd_dic["description"] = im_movie_info["description"]
                else:
                    if (upd_dic["description"]==""):
                        upd_dic["description"] = im_movie_info["description"]
                        
                if ("poster" not in upd_dic.keys()):
                    upd_dic["poster"] = im_movie_info["image"]
                else:
                    if (upd_dic["description"]==""):
                        upd_dic["description"] = im_movie_info["description"]
                upd_dic["poster"] = im_movie_info["image"]
                  
           
            
            #load updated movie
            self.db["movies"].find_one_and_update(movie,{'$set':upd_dic})
            
        
        #update index
        print("\nupdate effettuato\n")
        skipIdx+=nrows
        f=open("UpdateIndex.txt", "w")
        f.write(str(skipIdx))
        f.close()
        
         
if __name__ == "__main__":#test
    """INIT"""
    a = MongoManager()
    a.SetDBName('moviedb')
    a.PrintCollectsInDb()
    """"""
    
    a.UpdateMongoMovies('movies',1)#AGGIORNA K MOVIES (k=1)
    """stampa k film (k=1)"""
    """
    c1 = a.getCollection('movies',2)
    a.PrintCollection(c1)
    """
    #a.ServerStatus()