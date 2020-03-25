#!/usr/bin/env python
# coding: utf-8

# In[1]:


import pandas as pd
import numpy as np
from config import dataset_location, parse_dataset_output


# In[2]:


title_akas=pd.read_csv(dataset_location+'title.akas.tsv.gz', delimiter='\t')


# In[3]:


title_basics=pd.read_csv(dataset_location+'title.basics.tsv.gz', delimiter='\t')


# In[4]:


#only italians
title_ita=title_akas.loc[title_akas['region']=="IT"]
title_eng=title_akas.loc[title_akas['region']=="US"]


# In[5]:


title_ita.drop_duplicates(subset ="titleId", keep = 'first', inplace = True)
title_eng.drop_duplicates(subset ="titleId", keep = 'first', inplace = True)


# In[6]:


#join ita e basics
title_joined=title_ita.set_index('titleId').join(title_basics.set_index('tconst')).join(title_eng.set_index('titleId'), rsuffix="_eng")
title_joined.index.name = 'titleId'


# In[7]:


#only movies
title_movies_ita=title_joined.loc[title_joined['titleType']=="movie"]
title_movies_ita


# In[8]:


title_movies_ita.columns


# In[9]:


#Movie{id, title, original_title, runtime, country, year, characters, directors, genres, ratings, tot_rating}
#remove useless columns
title_movies_ita=title_movies_ita.drop(['ordering', 'region', 'language', 'types', 'attributes',
       'isOriginalTitle', 'titleType', 'primaryTitle', 
       'isAdult', 'endYear',
       'ordering_eng', 'region_eng', 'language_eng', 'types_eng',
       'attributes_eng', 'isOriginalTitle_eng'], axis=1)
title_movies_ita


# In[10]:


title_principals=pd.read_csv(dataset_location+'title.principals.tsv.gz', delimiter='\t')


# In[11]:


title_crew=pd.read_csv(dataset_location+'title.crew.tsv.gz', delimiter='\t')


# In[12]:


name_basics=pd.read_csv(dataset_location+'name.basics.tsv.gz', delimiter='\t')


# In[13]:


#join film e registi
title_directors=title_movies_ita.join(title_crew.set_index('tconst'))
title_directors


# In[14]:


#remove writers column
title_directors.drop(['writers'], axis=1)


# In[15]:


#put 1 director in each column
dir_split=title_directors["directors"].str.split(",", expand = True)
dir_split


# In[16]:


#save only the first 3 directors for each movie
for x in range(3, 73):
    dir_split.drop([x], axis=1, inplace=True)


# In[17]:


dir_split=dir_split.replace('\\N', np.nan)


# In[18]:


names=name_basics[['nconst', 'primaryName']]


# In[19]:


#reset index
dir_split.reset_index(level=0, inplace=True)


# In[20]:


#rename column
dir_split.columns = ['titleId', 'dir1', 'dir2', 'dir3']
dir_split


# In[21]:


#join directors with their names (1)
dir_name=dir_split.set_index('dir1').join(names.set_index('nconst'))
dir_name


# In[22]:


#reset index and rename columns (1)
dir_name=dir_name.rename(columns={'primaryName': 'name1'})
dir_name.reset_index(level=0, inplace=True)
dir_name=dir_name.rename(columns={'index': 'dir1'})
dir_name


# In[23]:


#join directors with their names (2)
dir_name=dir_name.set_index('dir2').join(names.set_index('nconst'))
dir_name


# In[24]:


#reset index and rename columns (2)
dir_name=dir_name.rename(columns={'primaryName': 'name2'})
dir_name.reset_index(level=0, inplace=True)
dir_name=dir_name.rename(columns={'index': 'dir2'})
dir_name


# In[25]:


#join directors with their names (3)
dir_name=dir_name.set_index('dir3').join(names.set_index('nconst'))
dir_name


# In[26]:


#reset index and rename columns (3)
dir_name=dir_name.rename(columns={'primaryName': 'name3'})
dir_name.reset_index(level=0, inplace=True)
dir_name=dir_name.rename(columns={'index': 'dir3'})
dir_name


# In[27]:


directors=dir_name


# In[28]:


#create couple (id, name)
directors['couple1'] = directors[['dir1', 'name1']].values.tolist()
directors['couple2'] = directors[['dir2', 'name2']].values.tolist()
directors['couple3'] = directors[['dir3', 'name3']].values.tolist()
directors


# In[29]:


#combine all the couple togheter
directors['directors'] = directors[['couple1', 'couple2', 'couple3']].values.tolist()
directors


# In[30]:


#join movies and directors
movies_dir=title_movies_ita.join(directors.set_index('titleId'))
movies_dir


# In[31]:


#remove useless column
movies_dir=movies_dir.drop(['dir1', 'dir2', 'dir3', 'name1', 'name2', 'name3', 'couple1', 'couple2', 'couple3'], axis=1)


# In[32]:


#only actors
actors=title_principals.loc[title_principals['category']=="actor"]


# In[33]:


#solo attori che hanno partecipato a film presenti nel database
movies_dir.reset_index(level=0, inplace=True)
actors_ita=actors.loc[actors.tconst.isin(movies_dir.titleId)]
actors_ita


# In[34]:


#remove square brackets
actors_ita['characters'] = actors_ita['characters'].apply(lambda x: x.replace('[','').replace(']','').replace('"',''))
actors_ita


# In[35]:


#change \N into null
actors_ita=actors_ita.replace('\\N', np.nan)


# In[36]:


#join actors with their names
actors_name=actors_ita.set_index('nconst').join(names.set_index('nconst'))
actors_name


# In[37]:


#create touples (character, name, id)
actors_name.reset_index(level=0, inplace=True)
actors_name['actor'] = actors_name[['characters', 'primaryName', 'nconst']].values.tolist()
actors_name


# In[38]:


#put all actors of a movie into an array
actors_grouped=actors_name.groupby('tconst').actor.apply(list).reset_index()
actors_grouped


# In[39]:


#join movies with actors
movies_final=movies_dir.set_index('titleId').join(actors_grouped.set_index('tconst'))
movies_final


# In[40]:


#change all \N into null
movies_final=movies_final.replace('\\N', np.nan)


# In[41]:


#remove film without a startYear
movies_final = movies_final[pd.notnull(movies_final['startYear'])]
movies_final


# In[42]:


movies_final.reset_index(level=0, inplace=True)
movies_final


# In[43]:


#rename and reorder columns
movies_final=movies_final[['titleId', 'title', 'title_eng', 'originalTitle', 'runtimeMinutes', 'startYear', 'actor', 'directors', 'genres']]
movies_final.columns=['tid', 'title_ita', 'title', 'originaltitle', 'runtime', 'year', 'characters', 'directors','genres']
movies_final


# In[44]:


#export as json
movies_final.to_json(parse_dataset_output, orient='records')

