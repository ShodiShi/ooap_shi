from player import Player
from player_ui import PlayerUI

# создаём плеер
player = Player()

# создаём и запускаем UI
ui = PlayerUI(player)
ui.render()