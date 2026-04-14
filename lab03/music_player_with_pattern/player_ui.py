import tkinter as tk
from tkinter import filedialog
import vlc
import os
import math
import random
import yt_dlp
from player import Player
from player_state import PlayingState, PausedState, StoppedState
from track import Track

# Цветовая палитра Spotify
BG      = "#121212"
BG2     = "#1a1a1a"
BG3     = "#282828"
ACCENT  = "#1db954"
ACCENT2 = "#1ed760"
TEXT    = "#ffffff"
TEXT2   = "#b3b3b3"
TEXT3   = "#535353"

class PlayerUI:
    def __init__(self, player):
        self.player = player
        self.vlc_instance = vlc.Instance()
        self.media_player = self.vlc_instance.media_player_new()
        self.angle = 0
        self.animation_running = False
        self.seeking = False
        self.shuffle = False
        self.mini_mode = False

        self.bars = [random.randint(5, 10) for _ in range(20)]
        self.bar_targets = [random.randint(5, 10) for _ in range(20)]

        self._scroll_offset = 0
        self._scroll_text = ""
        self._scroll_running = False

        self.window = tk.Tk()
        self.window.title("Music Player")
        self.window.geometry("420x750")
        self.window.configure(bg=BG)
        self.window.resizable(False, False)

        self.window.bind("<space>", lambda e: self.on_play_pause())
        self.window.bind("<Right>", lambda e: self.on_next())
        self.window.bind("<Left>", lambda e: self.on_prev())
        self.window.bind("<Up>", lambda e: self._volume_up())
        self.window.bind("<Down>", lambda e: self._volume_down())
        self.window.bind("<m>", lambda e: self.toggle_mini())

        self._show_splash()

    def _show_splash(self):
        self.splash = tk.Toplevel()
        self.splash.overrideredirect(True)
        self.splash.geometry("300x300+{}+{}".format(
            self.window.winfo_screenwidth() // 2 - 150,
            self.window.winfo_screenheight() // 2 - 150
        ))
        self.splash.configure(bg=BG)

        self.splash_canvas = tk.Canvas(
            self.splash, width=300, height=300,
            bg=BG, highlightthickness=0
        )
        self.splash_canvas.pack()

        self._splash_alpha = 0
        self._splash_angle = 0
        self._animate_splash()

    def _animate_splash(self):
        self.splash_canvas.delete("all")
        cx, cy = 150, 130
        r = 70

        for i in range(3):
            start = self._splash_angle + i * 120
            color = self._blend(BG3, ACCENT, (i + 1) / 3)
            self.splash_canvas.create_arc(
                cx - r - i*12, cy - r - i*12,
                cx + r + i*12, cy + r + i*12,
                start=start, extent=90,
                outline=color, width=3, style="arc"
            )

        self.splash_canvas.create_oval(
            cx-r, cy-r, cx+r, cy+r,
            fill=BG3, outline=ACCENT, width=2
        )
        self.splash_canvas.create_text(
            cx, cy, text="♫",
            fill=ACCENT, font=("Arial", 36)
        )
        self.splash_canvas.create_text(
            150, 220, text="Music Player",
            fill=ACCENT, font=("Arial", 18, "bold")
        )
        self.splash_canvas.create_text(
            150, 250, text="с паттерном «Состояние»",
            fill=TEXT2, font=("Arial", 10)
        )

        self._splash_angle = (self._splash_angle + 3) % 360
        self._splash_alpha += 5

        if self._splash_alpha < 80:
            self.splash.after(30, self._animate_splash)
        else:
            self.splash.after(1200, self._close_splash)

    def _close_splash(self):
        self.splash.destroy()
        self.window.deiconify()
        self._build_ui()
        self._update_progress()
        self._animate_visualizer()

    def _build_ui(self):
        self.window.withdraw()
        self.window.after(100, self.window.deiconify)

        self.canvas = tk.Canvas(
            self.window, width=200, height=200,
            bg=BG, highlightthickness=0
        )
        self.canvas.pack(pady=16)
        self._draw_disc(0)

        self.track_label = tk.Label(
            self.window, text="Нет трека",
            bg=BG, fg=TEXT,
            font=("Arial", 13, "bold")
        )
        self.track_label.pack()

        self.artist_label = tk.Label(
            self.window, text="",
            bg=BG, fg=TEXT2,
            font=("Arial", 10)
        )
        self.artist_label.pack(pady=2)

        self.status_label = tk.Label(
            self.window, text="Остановлен",
            bg=BG, fg=ACCENT,
            font=("Arial", 9)
        )
        self.status_label.pack()

        self.viz_canvas = tk.Canvas(
            self.window, width=380, height=50,
            bg=BG, highlightthickness=0
        )
        self.viz_canvas.pack(pady=4)

        progress_frame = tk.Frame(self.window, bg=BG)
        progress_frame.pack(fill=tk.X, padx=28, pady=4)

        self.time_current = tk.Label(
            progress_frame, text="0:00",
            bg=BG, fg=TEXT2, font=("Arial", 9)
        )
        self.time_current.pack(side=tk.LEFT)

        self.progress_var = tk.DoubleVar()
        self.progress_slider = tk.Scale(
            progress_frame,
            variable=self.progress_var,
            from_=0, to=100,
            orient=tk.HORIZONTAL,
            bg=BG, fg=TEXT,
            troughcolor=BG3,
            activebackground=ACCENT,
            highlightthickness=0,
            showvalue=False,
            sliderlength=12,
            length=280,
            command=self.on_seek
        )
        self.progress_slider.pack(side=tk.LEFT, padx=6)

        self.time_total = tk.Label(
            progress_frame, text="0:00",
            bg=BG, fg=TEXT2, font=("Arial", 9)
        )
        self.time_total.pack(side=tk.LEFT)

        vol_frame = tk.Frame(self.window, bg=BG)
        vol_frame.pack(pady=2)

        tk.Label(vol_frame, text="🔈", bg=BG,
                 fg=TEXT2, font=("Arial", 11)).pack(side=tk.LEFT)

        self.volume_slider = tk.Scale(
            vol_frame, from_=0, to=100,
            orient=tk.HORIZONTAL,
            bg=BG, fg=TEXT,
            troughcolor=BG3,
            activebackground=ACCENT,
            highlightthickness=0,
            showvalue=False,
            sliderlength=12,
            length=150,
            command=self.on_volume_change
        )
        self.volume_slider.set(70)
        self.volume_slider.pack(side=tk.LEFT, padx=4)

        tk.Label(vol_frame, text="🔊", bg=BG,
                 fg=TEXT2, font=("Arial", 11)).pack(side=tk.LEFT)

        controls = tk.Frame(self.window, bg=BG)
        controls.pack(pady=10)

        self.shuffle_btn = tk.Button(
            controls, text="⇀", command=self.toggle_shuffle,
            bg=BG3, fg=TEXT3,
            font=("Arial", 12), width=3,
            relief="flat", cursor="hand2",
            activebackground=ACCENT,
            activeforeground=BG, bd=0, pady=6
        )
        self.shuffle_btn.grid(row=0, column=0, padx=5)

        buttons_data = [
            ("⏮", self.on_prev, 12),
            ("▶", self.on_play, 16),
            ("⏸", self.on_pause, 14),
            ("⏹", self.on_stop, 14),
            ("⏭", self.on_next, 12),
        ]

        for i, (text, cmd, size) in enumerate(buttons_data):
            btn = tk.Button(
                controls, text=text, command=cmd,
                bg=BG3, fg=TEXT,
                font=("Arial", size), width=3,
                relief="flat", cursor="hand2",
                activebackground=ACCENT,
                activeforeground=BG,
                bd=0, pady=6
            )
            btn.grid(row=0, column=i+1, padx=5)
            btn.bind("<Enter>", lambda e, b=btn: b.config(bg=ACCENT, fg=BG))
            btn.bind("<Leave>", lambda e, b=btn: b.config(bg=BG3, fg=TEXT))

        mini_btn = tk.Button(
            controls, text="▭", command=self.toggle_mini,
            bg=BG3, fg=TEXT2,
            font=("Arial", 12), width=3,
            relief="flat", cursor="hand2",
            activebackground=ACCENT,
            activeforeground=BG, bd=0, pady=6
        )
        mini_btn.grid(row=0, column=6, padx=5)
        mini_btn.bind("<Enter>", lambda e, b=mini_btn: b.config(bg=ACCENT, fg=BG))
        mini_btn.bind("<Leave>", lambda e, b=mini_btn: b.config(bg=BG3, fg=TEXT2))

        add_frame = tk.Frame(self.window, bg=BG)
        add_frame.pack(pady=6)

        for text, cmd, color in [
            ("+ Трек", self.on_add_track, ACCENT),
            ("+ Папка", self.on_add_folder, BG3),
            ("+ YouTube", self.on_add_url, BG3),
        ]:
            btn = tk.Button(
                add_frame, text=text, command=cmd,
                bg=color, fg=BG if color == ACCENT else TEXT,
                font=("Arial", 10, "bold"),
                relief="flat", cursor="hand2",
                padx=10, pady=5, bd=0,
                activebackground=ACCENT2,
                activeforeground=BG
            )
            btn.pack(side=tk.LEFT, padx=5)
            btn.bind("<Enter>", lambda e, b=btn: b.config(bg=ACCENT2, fg=BG))
            btn.bind("<Leave>", lambda e, b=btn, c=color,
                     t=BG if color == ACCENT else TEXT: b.config(bg=c, fg=t))

        tk.Label(self.window, text="П Л Е Й Л И С Т",
                 bg=BG, fg=TEXT3,
                 font=("Arial", 8, "bold")).pack(pady=4)

        list_frame = tk.Frame(self.window, bg=BG2, bd=0)
        list_frame.pack(fill=tk.X, padx=20, pady=2)

        scrollbar = tk.Scrollbar(list_frame, bg=BG3,
                                  troughcolor=BG2,
                                  activebackground=ACCENT,
                                  width=6, bd=0)
        scrollbar.pack(side=tk.RIGHT, fill=tk.Y)

        self.track_listbox = tk.Listbox(
            list_frame,
            bg=BG2, fg=TEXT2,
            font=("Arial", 10),
            relief="flat",
            selectbackground=BG3,
            selectforeground=ACCENT,
            height=7, width=44,
            activestyle="none",
            bd=0,
            yscrollcommand=scrollbar.set
        )
        self.track_listbox.pack(side=tk.LEFT, fill=tk.BOTH)
        scrollbar.config(command=self.track_listbox.yview)
        self.track_listbox.bind("<Double-Button-1>", self.on_select_track)

        tk.Label(self.window,
                 text="Пробел: play/pause  |  ←→: треки  |  ↑↓: громкость  |  M: мини",
                 bg=BG, fg=TEXT3, font=("Arial", 8)).pack(pady=4)

    def _draw_disc(self, angle):
        self.canvas.delete("all")
        cx, cy, r = 100, 100, 88

        self.canvas.create_oval(
            cx-r, cy-r, cx+r, cy+r,
            fill=BG3, outline=ACCENT, width=2
        )

        for i in range(7):
            ri = 20 + i * 10
            color = self._blend(BG3, ACCENT, (7 - i) / 14)
            self.canvas.create_oval(
                cx-ri, cy-ri, cx+ri, cy+ri,
                fill="", outline=color, width=1
            )

        if self.animation_running:
            for j in range(6):
                rad = math.radians(angle - j * 7)
                x = cx + 65 * math.cos(rad)
                y = cy + 65 * math.sin(rad)
                size = 8 - j
                alpha = 1.0 - j * 0.16
                color = self._blend(BG3, ACCENT2, alpha)
                self.canvas.create_oval(
                    x-size, y-size, x+size, y+size,
                    fill=color, outline=""
                )

        self.canvas.create_oval(
            cx-16, cy-16, cx+16, cy+16,
            fill=BG, outline=ACCENT, width=2
        )
        self.canvas.create_oval(
            cx-4, cy-4, cx+4, cy+4,
            fill=ACCENT, outline=""
        )

    def _animate_disc(self):
        if not self.animation_running:
            return
        self.angle = (self.angle + 2) % 360
        self._draw_disc(self.angle)
        self.window.after(16, self._animate_disc)

    def _start_animation(self):
        if not self.animation_running:
            self.animation_running = True
            self._animate_disc()

    def _stop_animation(self):
        self.animation_running = False
        self._draw_disc(self.angle)

    def _animate_visualizer(self):
        if not hasattr(self, 'viz_canvas'):
            return
        self.viz_canvas.delete("all")
        cx = 190
        n = len(self.bars)
        bar_w = 10
        gap = 9

        for i in range(n):
            if self.bars[i] < self.bar_targets[i]:
                self.bars[i] = min(self.bars[i] + 3, self.bar_targets[i])
            else:
                self.bars[i] = max(self.bars[i] - 2, self.bar_targets[i])

            if self.bars[i] == self.bar_targets[i]:
                if isinstance(self.player.state, PlayingState):
                    self.bar_targets[i] = random.randint(8, 45)
                else:
                    self.bar_targets[i] = random.randint(3, 8)

            h = self.bars[i]
            x = cx - (n // 2) * (bar_w + gap) + i * (bar_w + gap)
            y_bottom = 45
            color = self._blend(ACCENT, ACCENT2, i / n)
            self.viz_canvas.create_rectangle(
                x, y_bottom - h, x + bar_w, y_bottom,
                fill=color, outline=""
            )

        self.window.after(40, self._animate_visualizer)

    def _start_scroll(self, text):
        self._scroll_text = text + "     "
        self._scroll_offset = 0
        self._scroll_running = True
        self._scroll_tick()

    def _scroll_tick(self):
        if not self._scroll_running:
            return
        text = self._scroll_text
        display = (text * 2)[self._scroll_offset:self._scroll_offset + 32]
        self.track_label.config(text=display)
        self._scroll_offset = (self._scroll_offset + 1) % len(text)
        self.window.after(130, self._scroll_tick)

    def toggle_shuffle(self):
        self.shuffle = not self.shuffle
        self.shuffle_btn.config(fg=ACCENT if self.shuffle else TEXT3)

    def toggle_mini(self):
        if not self.mini_mode:
            self.window.geometry("420x120")
            self.mini_mode = True
        else:
            self.window.geometry("420x750")
            self.mini_mode = False

    def _blend(self, color1, color2, t):
        t = max(0, min(1, t))
        r1, g1, b1 = int(color1[1:3], 16), int(color1[3:5], 16), int(color1[5:7], 16)
        r2, g2, b2 = int(color2[1:3], 16), int(color2[3:5], 16), int(color2[5:7], 16)
        r = int(r1 + (r2 - r1) * t)
        g = int(g1 + (g2 - g1) * t)
        b = int(b1 + (b2 - b1) * t)
        return f"#{r:02x}{g:02x}{b:02x}"

    def _format_time(self, ms):
        if ms <= 0:
            return "0:00"
        s = ms // 1000
        return f"{s // 60}:{s % 60:02d}"

    def _volume_up(self):
        val = min(100, self.volume_slider.get() + 5)
        self.volume_slider.set(val)
        self.media_player.audio_set_volume(val)

    def _volume_down(self):
        val = max(0, self.volume_slider.get() - 5)
        self.volume_slider.set(val)
        self.media_player.audio_set_volume(val)

    def _update_progress(self):
        if isinstance(self.player.state, PlayingState):
            current = self.media_player.get_time()
            total = self.media_player.get_length()
            if total > 0 and not self.seeking:
                self.progress_var.set(current / total * 100)
                self.time_current.config(text=self._format_time(current))
                self.time_total.config(text=self._format_time(total))
                if current >= total - 600:
                    self.on_next()
        self.window.after(500, self._update_progress)

    def on_seek(self, val):
        total = self.media_player.get_length()
        if total > 0:
            self.media_player.set_time(int(float(val) / 100 * total))

    def on_volume_change(self, val):
        self.media_player.audio_set_volume(int(val))

    def on_play_pause(self):
        if isinstance(self.player.state, PlayingState):
            self.on_pause()
        else:
            self.on_play()

    def on_play(self):
        track = self.player.playlist.get_track()
        if track is None:
            return
        if isinstance(self.player.state, PausedState):
            self.media_player.pause()
        elif isinstance(self.player.state, StoppedState):
            media = self.vlc_instance.media_new(track.get_file_path())
            self.media_player.set_media(media)
            self.media_player.play()
            self.media_player.audio_set_volume(self.volume_slider.get())
        self.player.play()
        self._update_ui("▶ Играет")
        self._start_animation()

    def on_pause(self):
        if isinstance(self.player.state, PlayingState):
            self.media_player.pause()
            self.player.pause()
            self._update_ui("⏸ Пауза")
            self._stop_animation()

    def on_stop(self):
        self.media_player.stop()
        self.player.stop()
        self._update_ui("⏹ Остановлен")
        self._stop_animation()
        self.progress_var.set(0)
        self.time_current.config(text="0:00")
        self.time_total.config(text="0:00")

    def on_next(self):
        self.media_player.stop()
        self.player.stop()
        if self.shuffle:
            self.player.playlist.current_index = random.randint(
                0, self.player.playlist.size() - 1
            )
        else:
            self.player.next()
        self.on_play()

    def on_prev(self):
        self.media_player.stop()
        self.player.stop()
        self.player.prev()
        self.on_play()

    def on_add_track(self):
        file_path = filedialog.askopenfilename(
            filetypes=[("Аудио файлы", "*.mp3 *.wav *.ogg *.flac")]
        )
        if file_path:
            title = os.path.splitext(os.path.basename(file_path))[0]
            track = Track(title, file_path, "Неизвестен")
            self.player.add_track(track)
            self.track_listbox.insert(tk.END, f"  {title}")

    def on_add_folder(self):
        folder_path = filedialog.askdirectory()
        if folder_path:
            for file in sorted(os.listdir(folder_path)):
                if file.endswith((".mp3", ".wav", ".ogg", ".flac")):
                    file_path = os.path.join(folder_path, file)
                    title = os.path.splitext(file)[0]
                    track = Track(title, file_path, "Неизвестен")
                    self.player.add_track(track)
                    self.track_listbox.insert(tk.END, f"  {title}")

    def on_add_url(self):
        url_window = tk.Toplevel(self.window)
        url_window.title("Найти на YouTube")
        url_window.geometry("400x150")
        url_window.configure(bg=BG)
        url_window.resizable(False, False)

        tk.Label(url_window, text="Введи название трека:",
                 bg=BG, fg=TEXT2, font=("Arial", 10)).pack(pady=10)

        entry = tk.Entry(url_window, width=45, bg=BG3, fg=TEXT,
                         font=("Arial", 10), relief="flat",
                         insertbackground=TEXT)
        entry.pack(padx=20)
        entry.focus()

        status = tk.Label(url_window, text="",
                          bg=BG, fg=TEXT2, font=("Arial", 9))
        status.pack(pady=4)

        def search():
            query = entry.get().strip()
            if not query:
                return
            status.config(text="Ищем на YouTube...")
            url_window.update()

            try:
                ydl_opts = {
                    "format": "bestaudio/best",
                    "quiet": True,
                    "noplaylist": True,
                    "default_search": "ytsearch1",
                }
                with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                    info = ydl.extract_info(query, download=False)
                    if "entries" in info:
                        info = info["entries"][0]
                    stream_url = info["url"]
                    title = info.get("title", query)
                    artist = info.get("uploader", "YouTube")

                track = Track(title, stream_url, artist)
                self.player.add_track(track)
                self.track_listbox.insert(tk.END, f"  {title}")
                url_window.destroy()

            except Exception as e:
                status.config(text=f"Ошибка: {str(e)[:40]}")

        tk.Button(url_window, text="Найти и добавить", command=search,
                  bg=ACCENT, fg=BG, font=("Arial", 10, "bold"),
                  relief="flat", cursor="hand2", padx=10, pady=4).pack(pady=4)

        entry.bind("<Return>", lambda e: search())

    def on_select_track(self, event):
        index = self.track_listbox.curselection()
        if index:
            self.media_player.stop()
            self.player.stop()
            self.player.playlist.current_index = index[0]
            self.on_play()

    def _update_ui(self, status):
        track = self.player.playlist.get_track()
        if track:
            title = track.get_title()
            if len(title) > 28:
                self._scroll_running = False
                self.window.after(200, lambda t=title: self._start_scroll(t))
            else:
                self._scroll_running = False
                self.track_label.config(text=title)
            self.artist_label.config(text=track.get_artist())
        self.status_label.config(text=status)
        self.track_listbox.selection_clear(0, tk.END)
        self.track_listbox.selection_set(self.player.playlist.current_index)
        self.track_listbox.see(self.player.playlist.current_index)

    def render(self):
        self.window.mainloop()