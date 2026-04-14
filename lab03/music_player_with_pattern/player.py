from playlist import Playlist
from player_state import StoppedState

class Player:
    def __init__(self):
        self.playlist = Playlist()
        self.state = StoppedState()  

    def set_state(self, state):
        self.state = state  

    def play(self):
        self.state.play(self)  

    def pause(self):
        self.state.pause(self)

    def stop(self):
        self.state.stop(self)

    def next(self):
        self.state.next(self)

    def prev(self):
        self.state.prev(self)

    def add_track(self, track):
        self.playlist.add_track(track)
        print(f"Добавлен трек: {track.get_artist()} - {track.get_title()}")