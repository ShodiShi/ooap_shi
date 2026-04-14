class Track:
    def __init__(self, title, file_path, artist):
        self.title = title          
        self.file_path = file_path  
        self.artist = artist        

    def get_title(self):
        return self.title

    def get_file_path(self):
        return self.file_path

    def get_artist(self):
        return self.artist