from abc import ABC, abstractmethod


class PlayerState(ABC):

    @abstractmethod
    def play(self, player):
        pass

    @abstractmethod
    def pause(self, player):
        pass

    @abstractmethod
    def stop(self, player):
        pass

    @abstractmethod
    def next(self, player):
        pass

    @abstractmethod
    def prev(self, player):
        pass



class StoppedState(PlayerState):

    def play(self, player):
        track = player.playlist.get_track()
        if track is None:
            print("Плейлист пуст")
            return
        player.set_state(PlayingState())  # переход в состояние Playing
        print(f"Играет: {track.get_artist()} - {track.get_title()}")

    def pause(self, player):
        print("Нельзя поставить на паузу — плеер остановлен")

    def stop(self, player):
        print("Плеер уже остановлен")

    def next(self, player):
        player.playlist.next_track()

    def prev(self, player):
        player.playlist.prev_track()



class PlayingState(PlayerState):

    def play(self, player):
        print("Уже играет")

    def pause(self, player):
        player.set_state(PausedState())  # переход в состояние Paused
        print("Пауза")

    def stop(self, player):
        player.set_state(StoppedState())  # переход в состояние Stopped
        print("Остановлено")

    def next(self, player):
        player.playlist.next_track()
        track = player.playlist.get_track()
        if track:
            print(f"Следующий трек: {track.get_artist()} - {track.get_title()}")

    def prev(self, player):
        player.playlist.prev_track()
        track = player.playlist.get_track()
        if track:
            print(f"Предыдущий трек: {track.get_artist()} - {track.get_title()}")



class PausedState(PlayerState):

    def play(self, player):
        player.set_state(PlayingState())  # возобновляем — переход в Playing
        track = player.playlist.get_track()
        print(f"Возобновлено: {track.get_artist()} - {track.get_title()}")

    def pause(self, player):
        print("Уже на паузе")

    def stop(self, player):
        player.set_state(StoppedState())  # переход в Stopped
        print("Остановлено")

    def next(self, player):
        player.playlist.next_track()

    def prev(self, player):
        player.playlist.prev_track()