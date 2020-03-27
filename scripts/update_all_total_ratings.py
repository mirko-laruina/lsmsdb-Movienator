import multiprocessing

from pymongo import MongoClient

import config

def update_total_rating(movie):
    client = MongoClient(config.mongo_uri)
    db = client[config.mongo_db]
    
    print(movie['_id'])

    movie_upd = db["movies"].find_one({
                    '_id': movie['_id']
                }, 
                projection = ['ratings']
    )

    rating_sum = sum([float(r['avgrating'])*float(r['weight']) for r in movie_upd['ratings']])
    denominator = len(movie_upd['ratings'])
    
    if denominator != 0:
        total_rating = rating_sum/denominator

        db["movies"].find_one_and_update({
                '_id': movie['_id']
            }, 
            {
                '$set': {
                    'total_rating': total_rating
                }
            }
        )
    

if __name__ == "__main__":
    client = MongoClient(config.mongo_uri)
    db = client[config.mongo_db]

    pool = multiprocessing.Pool(4)

    movies = db['movies'].find({}, { '_id': 1 })

    pool.map(update_total_rating, movies)
