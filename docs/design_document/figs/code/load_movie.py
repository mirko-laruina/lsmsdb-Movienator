def LoadMovie(self,movieId):
    """valid format only for imdb,mymovies
    ---req_movie_path = self.source + str(movieId)
    ---req_movie_path = https://www.mymovies.it/film/yyyy/title/

    movieId can be an integer ->IMDB, a string like 
    'https://www.mymovies.it/film/yyyy/title/'->mymovies
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
    for key in MovieScraper.Attributes + MovieScraper.EXTRA_ATTRIBUTES:
        if key not in req_movie.keys():
            req_movie[key]= None
    
    nt = {"source":str(self.source), "movie":{} } #new dict element
    
    #listing attributes separately
    for key in MovieScraper.Attributes + MovieScraper.EXTRA_ATTRIBUTES:
        # gestire il name nel formato opportuno per ricavare una lista di 
        # nomi compatibile rottent e mymovies
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