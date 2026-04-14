from track import Track

class Playlist:
    def __init__(self):
        self.tracks = []        
        self.current_index = 0  

    def add_track(self, track):
        self.tracks.append(track)  

    def remove_track(self, track):
        self.tracks.remove(track)  

    def get_track(self):
        if len(self.tracks) == 0:  
            return None
        return self.tracks[self.current_index]  

    def next_track(self):
        if self.current_index < len(self.tracks) - 1:
            self.current_index += 1  

    def prev_track(self):
        if self.current_index > 0:
            self.current_index -= 1  

    def size(self):
        return len(self.tracks)  