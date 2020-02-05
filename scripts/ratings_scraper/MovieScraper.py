# -*- coding: utf-8 -*-

import json
import pprint

"attualmente in grado di gestire lo scrape da imdb"


import requests
from bs4 import BeautifulSoup

def get_ld_json(url: str) -> dict:#this method return parsed info as a dict by scraped page
    parser = "html.parser"
    req = requests.get(url)
    soup = BeautifulSoup(req.text, parser)
    return json.loads("".join(soup.find("script", {"type":"application/ld+json"}).contents))

def makeId(k):
    Id='tt'
    for x in range(0,7-len(str(k))):
        Id = Id+'0'
    Id += str(k)
    return Id
    #print(Id)
    
    
web_sources = {
        "imdb": "https://www.imdb.com/title/",    #idfilm : ttxxxxxxx/
        "mymovies": "https://www.mymovies.it/film/",    #idfilm : year/movie_titlemovietitil/
        "RottenTomatoes": "https://www.rottentomatoes.com/m/"     #idfilm : movie_title/
        }


class MovieScraper:

    #source itemAttr
    Attributes = ["name","image","description","datePublished","aggregateRating"]
    
    def __init__(self,sourcename):
        
        mysource = web_sources.get(sourcename,None)
        if mysource == None :
            raise Exception("Sorry, requested source not available")
        else:
            self.source = mysource
            
        for key in MovieScraper.Attributes:
            exec("self."+key+" = []" ) in locals()
        self.moviesInfo = []#list of dictionaries
            
    def get_source(self):
        return self.source
    
    def change_source(self,newsource):
        oldsource= self.source
        self.source = web_sources.get(newsource,None)
        if self.source == None :
            print('source not updated')
            self.source = oldsource
        else:
            print('source updated')
            
    def store_new_movies(self):
        #append new elements to file in json
        #fo = open("Movies.json", "a+")
        #new_json_docs = json.dumps(self.LoadMovieInfo)
        with open("Movies.json", 'a+') as outfile:
            for x in range(0,len(self.moviesInfo),1):
                #json.dump(self.moviesInfo[x], outfile)
                outfile.write(json.dumps(self.moviesInfo[x]))
                outfile.write("\n")#add newline between documents
        print('\nData stored\n')
        self.moviesInfo.clear()
        #clean data structure
            
    def LoadNextKMovies(self,k=10):
        
        try:
            f=open("dataIndex.txt", "r")
            self.indexm=int(f.read())
        except OSError as oe:
            self.indexm=0
        finally:
            print('scarico dalla posizione: '+str(self.indexm))
            ack = input('\ndigita \'s\' per continuare\n')
            if (ack!='s'):
                if 'f' in locals():
                    f.close()
                return
            
            if 'f' in locals():
                f.close()
            
        for x in range(self.indexm,self.indexm + k,1):
            movieId = makeId(x)
            print(movieId)
            try:
                self.LoadMovieInfo(movieId)
            except AttributeError as Ae:
                print(movieId+' : movie not found\n')
                continue
            
        #update data index
        self.indexm = int(self.indexm) + k
        f=open("dataIndex.txt", "w")
        f.write(str(self.indexm))
        f.close()
        
        self.store_new_movies()#appends all self.movies info in json format file
            
        
    def LoadMovieInfo(self,movieId)->tuple:#goal:make a list of tuples for each movie: (Name, Description,OutDate,urlPic)
        """valid format only for imdb
        ---req_movie_path = self.source + str(movieId)
        """
        req_movie_path = self.source + str(movieId)
        req_movie = get_ld_json(req_movie_path)
        
        #miss values manager
        for key in MovieScraper.Attributes:
            if key not in req_movie.keys():
                req_movie[key]= None
        
        nt = {} #new dict element
        
        #listing attributes separately
        for key in MovieScraper.Attributes:
            #gestire il name nel formato opportuno per ricavare una lista di nomi compatibile rottent e mymovies
            exec("self."+key+".append(req_movie[key])" ) in locals()
            nt[key] = req_movie[key]
        """     
            if key !='aggregateRating':
                nt.append(req_movie[key])
            else:
                idx=self.aggregateRating[0]
                nt.append(idx['ratingCount'])
                nt.append(idx['bestRating'])
                nt.append(idx['worstRating'])
                nt.append(idx['ratingValue'])
        """       
        #listing attributes in tuples
        """
        new_tuple= tuple(nt)
        self.moviesInfo.append(new_tuple)
        """
        #getting attributes movie matrice
        new_movie_row = nt
        self.moviesInfo.append(new_movie_row)
        
       
        
    
    def read_k_stored_movies(self,k=5):
        try:
            with open("Movies.json", 'r') as outfile:
                ack='s'
                while (ack =='s'):
                    for i in range(0,k,1):
                        doc_i = outfile.readline()
                        if doc_i =='':
                            print('lettura completata')
                            return
                        pprint.pprint(doc_i)
                    ack=input('\ndigita \'s\' per leggere i successivi k film scaricati\n')
        except FileNotFoundError as fnf:
            print(fnf)
            print('Il file non è presente o non è stato ancora generato')
            return
            
        
if __name__ == "__main__":#test : settare i parametri k in base al volume richiesto
    a = MovieScraper('imdb')
    """alla prima chiamata se non presente viene generato un file in cui viene salvato l'indice di scrape (un intero) """
    """scrape dei successivi k (=2) movies """
    a.LoadNextKMovies(2)  
    print('-----\n')
    """legge il file json a gruppi di k (=2) elementi"""
    a.read_k_stored_movies(2)
    print('"""""""\n')
    """la lettura riparte da inizio file """
    a.read_k_stored_movies(2)
