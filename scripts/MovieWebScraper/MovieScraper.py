# -*- coding: utf-8 -*-

import json
#instal package-> python -m pip install pymongo

"attualmente in grado di gestire lo scrape da imdb"


import requests
from bs4 import BeautifulSoup


        
def makeId(k):
    
    Id='tt'
    for x in range(0,7-len(str(k))):
        Id = Id+'0'
    Id += str(k)
    return Id
    #print(Id)
    
def get_movie_from_local_db(movieIdx):
        movie_dict = {}
        fp = open("Movies.json",'r')
        for i, line in enumerate(fp):
            if i == movieIdx:
                movie_dict = json.loads(line)
        fp.close()
        return movie_dict

def get_imdb_movies_from_local_db(lb,hb):
        movies = []
        #append dict elements
        fp = open("Movies.json",'r')
        for i, line in enumerate(fp):
            if i >= lb and i<hb:
                movie_dict = json.loads(line)
                if movie_dict["source"]=="imdb":
                    movies.append(movie_dict)
                else:
                    hb+=1
        fp.close()
        if (len(movies)!=0):
            return movies
        else:
            return None
    
def getMyMovieQuery(source_path,movie):#returns the path string to reach the movie at passed source
    m_title = movie["title_ita"]#movie["movie"]["name"]#CAMBIA CON L'ARGOMENTO PASSATO IN INPUT
    source_path += m_title
    return source_path
 
def FindMovieUrlByQuery(source:str,movieReq:dict):#mymovies,rottentomato only
        #source control
        mysource_p = web_sources.get(source,None)
        if mysource_p == None or mysource_p == web_sources['imdb'] :
            raise Exception("Sorry, requested source not available")
        else:
            parser = "html.parser"
            req_movie_path = getMyMovieQuery(mysource_p,movieReq) #( source=="mymovies" ? getMyMovieQuery(mysource_p,movieReq) : getRottenTPath(mysource_p,movieReq) )
            req_movie = requests.get((req_movie_path))
            soup = BeautifulSoup(req_movie.text, parser)
            movie_details = json.loads(soup.get_text())
            #print(movie_details)
            #print(movie_details)
            if movie_details["esito"]=="SUCCESS" and 'film' in movie_details['risultati']:
                addressMovieUrl = movie_details['risultati']['film']['elenco'][1]['url']
            else:
                addressMovieUrl = None
            #print(addressMovieUrl)
            return str(addressMovieUrl)

def normalize_json_string(string):
    """
    Replaces newlines (\n) within quotes with escaped ones and removes carriage
    returns (\r).
    """

    result = ""
    between_quotes = False
    for i, c in enumerate(string):
        if c == '"' and string[i-1] != '\\':
            between_quotes = not between_quotes
            result += c
        elif between_quotes and c == '\n':
            result += '\\n'
        elif between_quotes and c == '\r':
            continue
        else:
            result += c    
    return result
    
    
    
    
web_sources = {
        "imdb": "https://www.imdb.com/title/",    #idfilm : ttxxxxxxx/
        "mymovies": "https://www.mymovies.it/ricerca/ricerca.php?limit=true&q=",    #idfilm : year/movie_titlemovietitil/
        "RottenTomatoes": "https://www.rottentomatoes.com/m/"     #idfilm : movie_title/
        }


class MovieScraper:

    #source itemAttr
    Attributes = ["name","genre","image","description","datePublished","aggregateRating"]
    
    def __init__(self,sourcename="imdb"):
        
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
            
        
    def get_ld_json(self, url: str) -> dict:#this method return parsed info as a dict by scraped page
        parser = "html.parser"
        req = requests.get(url)
        soup = BeautifulSoup(req.text, parser)
        
        if self.source == "imdb":
            ld_json = soup.find("script", {"type":"application/ld+json"})
            if ld_json is not None:
                return json.loads(normalize_json_string("".join(ld_json.contents)))
            else:
                return None
        elif self.source == "mymovies":
            json_scripts = soup.findAll("script", {"type":"application/ld+json"})
            for script in json_scripts:
                obj = json.loads(normalize_json_string(script.getText()))
                if ("name" in obj.keys() and "genre" in obj.keys() ):
                    return obj#return json.loads("".join( soup.findAll("script", {"type":"application/ld+json"})[2] ))
                else:
                    continue
            return None
        
    def LoadMyMovieByIMDB(self,lb,hb):
        # get_movies_from_local_db
        print('\nScaricando film da mymovie...\n')
        moviesToFind = get_imdb_movies_from_local_db(lb,hb)
        
        if (moviesToFind==None):
            print("\nMyMovies: Nessun film da importare ")
            return
        
        for movie in moviesToFind:
            source_url = FindMovieUrlByQuery("mymovies",movie)
            self.LoadMovie(source_url)
            
        self.store_new_movies()#appends all self.movies info in json format file
    
    def updateScrapeIndex(self,newidx):
        with open("dataIndex.txt", "w") as f:
            f.write(str(newidx))
        
        
    def LoadNextKMovies(self,k=10):#imdb only
        try:
            f=open("dataIndex.txt", "r")
            self.indexm=int(f.read())
        except OSError as oe:
            self.indexm = 0
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
            #print(movieId)
            try:
                self.LoadMovie(movieId)
            except AttributeError as Ae:
                print(movieId+' : movie not found\n')
                continue
            
        #update data index
        self.indexm = int(self.indexm) + k
        f=open("dataIndex.txt", "w")
        f.write(str(self.indexm))
        f.close()
        
        self.store_new_movies()#appends all self.movies info in json format file
            
        
        
        
    def LoadMovie(self,movieId):#movieId can be an integer ->IMDB, a string like 'https://www.mymovies.it/film/yyyy/title/'->mymovies or ----- ->rottentom
        """valid format only for imdb,mymovies
        ---req_movie_path = self.source + str(movieId)
        ---req_movie_path = https://www.mymovies.it/film/yyyy/title/
        """
        if movieId.startswith("https://www.mymovies.it/"):
            self.source = "mymovies" 
            """add tomato"""
        else:
            self.source = "imdb"
            movieId = web_sources[self.source] + str(movieId)
        #getting json results
        req_movie = self.get_ld_json(movieId)
        if req_movie == None:
            return
        #print(req_movie)
        
        #miss values manager
        for key in MovieScraper.Attributes:
            if key not in req_movie.keys():
                req_movie[key]= None
        
        nt = {"source":str(self.source), "movie":{} } #new dict element
        
        #listing attributes separately
        for key in MovieScraper.Attributes:
            #gestire il name nel formato opportuno per ricavare una lista di nomi compatibile rottent e mymovies
            exec("self."+key+".append(req_movie[key])" ) in locals()
            nt["movie"][key] = req_movie[key]      
        #listing attributes in tuples
        """
        new_tuple= tuple(nt)
        self.moviesInfo.append(new_tuple)
        """
        #getting attributes movie matrice
        #print(nt)
        new_movie_doc = nt
        self.moviesInfo.append(new_movie_doc)
        return new_movie_doc
        
       
        
    
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
                        print(doc_i)
                    ack=input('\ndigita \'s\' per leggere i successivi '+str(k)+' film scaricati\n')
        except FileNotFoundError as fnf:
            print(fnf)
            print('Il file non è presente o non è stato ancora generato')
            return
    
    """"""""""""
    def getMovieFromMyMovie(self,movie_info):
        film_url = FindMovieUrlByQuery("mymovies",movie_info)
        if film_url is not None:
            return self.LoadMovie(film_url)
        else:
            return None
    
        
if __name__ == "__main__":#test
    a = MovieScraper('mymovies',)
    """alla prima chiamata se non presente viene generato un file in cui viene salvato l'indice di scrape (un intero) di default a partire da 0 , può essere modificato a piacere da file dataIndex """
    """scrape dei successivi k (e.g.=20) movies da imdb (indice 0) """
   # a.LoadNextKMovies(20)
    
    """test load mymovies data"""
    #a.updateScrapeIndex(8360000)
    #a.LoadNextKMovies(20)
    """ """

    print('-----\n')
    
    """scarico da mymovies dalla posizione 0 i successivi 25 film che hanno source == imdb diventano il seed per ricercare i film su mymovies"""
    #a.LoadMyMovieByIMDB(0,25)#!!!mismatch di titoli: i film restituiti da mymovies non sono consistenti con quelli usati come indice (di imdb) !!!
    a.getMovieFromMyMovie({"title_ita":"labellaelabestia"})
    """leggo i risultati a blocchi di k elementi"""
   # a.read_k_stored_movies(10)
