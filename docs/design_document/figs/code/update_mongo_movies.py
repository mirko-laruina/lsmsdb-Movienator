def UpdateMongoMovies(self,coll_name,nrows=20):
    mysource_p = ms.web_sources.get("mymovies",None)
    if mysource_p == None :
        raise Exception("Sorry, requested source not available")
    
    # coll_iterator = self.getCollection(coll_name,nrows,skipIdx)####
    coll_iterator = self.getMoviesByLastScraped(nrows)
    
    for movie in coll_iterator:
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

        # scrape information from MyMovies -----------------------------------
        # [...] -> upd_dic
            
        # scrape information from IMDb ---------------------------------------
        # [...] -> upd_dic
        
        # update movie ratings -----------------------------------------------
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
        
            # merge new ratings with old ones
            # [...]
            # calculate new total_rating
            # [...]
            # update movie document with total rating andother info
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
