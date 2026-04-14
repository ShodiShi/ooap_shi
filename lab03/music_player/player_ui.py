import tkinter as tk
from tkinter import filedialog
import pygame
from player import Player
from track import Track


pygame.mixer.init()

class PlayerUI:
    def __init__(self, player):
        self.player = player  

        
        self.window = tk.Tk()
        self.window.title("Музыкальный плеер")
        self.window.geometry("400x500")
        self.window.configure(bg="#1e1e2e")

        self._build_ui() 

    def _build_ui(self):
        
        self.track_label = tk.Label(
            self.window,
            text="Нет трека",
            bg="#1e1e2e",
            fg="white",
            font=("Arial", 14, "bold"),
            wraplength=350
        )
        self.track_label.pack(pady=30)

        
        self.artist_label = tk.Label(
            self.window,
            text="",
            bg="#1e1e2e",
            fg="#aaaaaa",
            font=("Arial", 11)
        )
        self.artist_label.pack()

     
        self.status_label = tk.Label(
            self.window,
            text="Остановлен",
            bg="#1e1e2e",
            fg="#888888",
            font=("Arial", 10)
        )
        self.status_label.pack(pady=10)

        
        controls = tk.Frame(self.window, bg="#1e1e2e")
        controls.pack(pady=20)

        btn_style = {"bg": "#313244", "fg": "white", "font": ("Arial", 12),
                     "width": 4, "height": 1, "relief": "flat", "cursor": "hand2"}

        tk.Button(controls, text="⏮", command=self.on_prev, **btn_style).grid(row=0, column=0, padx=8)
        tk.Button(controls, text="▶", command=self.on_play, **btn_style).grid(row=0, column=1, padx=8)
        tk.Button(controls, text="⏸", command=self.on_pause, **btn_style).grid(row=0, column=2, padx=8)
        tk.Button(controls, text="⏹", command=self.on_stop, **btn_style).grid(row=0, column=3, padx=8)
        tk.Button(controls, text="⏭", command=self.on_next, **btn_style).grid(row=0, column=4, padx=8)

        
        tk.Button(
            self.window,
            text="+ Добавить трек",
            command=self.on_add_track,
            bg="#89b4fa",
            fg="#1e1e2e",
            font=("Arial", 11, "bold"),
            relief="flat",
            cursor="hand2",
            padx=10,
            pady=6
        ).pack(pady=10)

     
        tk.Label(self.window, text="Плейлист:", bg="#1e1e2e", fg="#aaaaaa",
                 font=("Arial", 10)).pack()

        self.track_listbox = tk.Listbox(
            self.window,
            bg="#313244",
            fg="white",
            font=("Arial", 10),
            relief="flat",
            selectbackground="#89b4fa",
            selectforeground="#1e1e2e",
            height=8,
            width=45
        )
        self.track_listbox.pack(pady=5)
        self.track_listbox.bind("<Double-Button-1>", self.on_select_track)

    def on_play(self):
        track = self.player.playlist.get_track()
        if track is None:
            return
        if self.player.is_paused:
            pygame.mixer.music.unpause()
            self.player.is_paused = False
            self.player.is_playing = True
        elif not self.player.is_playing:
            pygame.mixer.music.load(track.get_file_path())
            pygame.mixer.music.play()
            self.player.is_playing = True
            self.player.is_paused = False
        self._update_ui("▶ Играет")

    def on_pause(self):
        if self.player.is_playing and not self.player.is_paused:
            pygame.mixer.music.pause()
            self.player.is_paused = True
            self._update_ui("⏸ Пауза")

    def on_stop(self):
        pygame.mixer.music.stop()
        self.player.is_playing = False
        self.player.is_paused = False
        self._update_ui("⏹ Остановлен")

    def on_next(self):
        pygame.mixer.music.stop()
        self.player.is_playing = False
        self.player.next()
        self.on_play()

    def on_prev(self):
        pygame.mixer.music.stop()
        self.player.is_playing = False
        self.player.prev()
        self.on_play()

    def on_add_track(self):
        
        file_path = filedialog.askopenfilename(
            filetypes=[("Аудио файлы", "*.mp3 *.wav *.ogg")]
        )
        if file_path:
            
            title = file_path.split("/")[-1].replace(".mp3", "").replace(".wav", "").replace(".ogg", "")
            track = Track(title, file_path, "Неизвестен")
            self.player.add_track(track)
            self.track_listbox.insert(tk.END, f"{title}")

    def on_select_track(self, event):
        
        index = self.track_listbox.curselection()
        if index:
            pygame.mixer.music.stop()
            self.player.is_playing = False
            self.player.playlist.current_index = index[0]
            self.on_play()

    def _update_ui(self, status):
        track = self.player.playlist.get_track()
        if track:
            self.track_label.config(text=track.get_title())
            self.artist_label.config(text=track.get_artist())
        self.status_label.config(text=status)

    def render(self):
        self.window.mainloop()  