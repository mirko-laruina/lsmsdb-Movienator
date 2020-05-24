def get_ld_json(self, url: str) -> dict:
    '''
    This method return parsed info as a dict by scraped page
    '''

    parser = "html.parser"
    req = requests.get(url)
    print(req.encoding)
    if self.source == "mymovies":
        text = str(req.content, 'UTF-8', errors='replace')
    else:
        text = req.text
    soup = BeautifulSoup(text, parser)
    print(url)
    
    if self.source == "imdb":
        ld_json = soup.find("script", {"type":"application/ld+json"})
        if ld_json is not None:
            json_dict = json.loads(normalize_json_string("".join(ld_json.contents)))
            json_dict.update(self.find_additional_info_from_imdb(soup))
            return json_dict
        else:
            return None
    elif self.source == "mymovies":
        json_scripts = soup.findAll("script", {"type":"application/ld+json"})
        for script in json_scripts:
            obj = json.loads(normalize_json_string(script.getText()))
            if ("name" in obj.keys() and "genre" in obj.keys() ):
                return obj
            else:
                continue
        return None