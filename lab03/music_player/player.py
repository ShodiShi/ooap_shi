from playlist import Playlist  

class Player:
    def __init__(self):
        self.playlist = Playlist()  
        self.is_playing = False     
        self.is_paused = False      

    def play(self):
        if self.is_playing:
            print("Уже играет")     
            return
        if self.playlist.get_track() is None:
            print("Плейлист пуст") 
            return
        self.is_playing = True
        self.is_paused = False
        track = self.playlist.get_track()
        print(f"Играет: {track.get_artist()} - {track.get_title()}")

    def pause(self):
        if not self.is_playing:
            print("Ничего не играет")
            return
        if self.is_paused:
            
            self.is_paused = False
            track = self.playlist.get_track()
            print(f"Возобновлено: {track.get_artist()} - {track.get_title()}")
        else:
            
            self.is_paused = True
            print("Пауза")

    def stop(self):
        if not self.is_playing:
            print("Ничего не играет")
            return
        self.is_playing = False
        self.is_paused = False
        print("Остановлено")

    def next(self):
        self.playlist.next_track()
        if self.is_playing:
            track = self.playlist.get_track()
            print(f"Следующий трек: {track.get_artist()} - {track.get_title()}")

    def prev(self):
        self.playlist.prev_track()
        if self.is_playing:
            track = self.playlist.get_track()
            print(f"Предыдущий трек: {track.get_artist()} - {track.get_title()}")

    def add_track(self, track):
        self.playlist.add_track(track) 
        print(f"Добавлен трек: {track.get_artist()} - {track.get_title()}")