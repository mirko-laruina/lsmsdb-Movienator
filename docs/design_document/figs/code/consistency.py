def sync_movie(m):
    global graph

    movie = {k:v for k,v in m.items() if k in keep_movie_attrs}
    n = Node("Movie", **movie)
    graph.merge(n, "Movie", "_id")

def sync_user(u):
    global graph

    u['_id'] = u['_id'].__str__()
    user = {k:v for k,v in u.items() if k in keep_user_attrs}
    m = Node("User", **user)
    graph.merge(m, "User", "_id")

def sync_rating(r):
    global graph

    RATED = Relationship.type("RATED")

    u = graph.nodes.match("User", _id=r["_id"]["user_id"].__str__()).first()
    m = graph.nodes.match("Movie", _id=r["_id"]["movie_id"]).first()
    graph.merge(RATED(u,m, rating=r['rating'], date=r['date']))