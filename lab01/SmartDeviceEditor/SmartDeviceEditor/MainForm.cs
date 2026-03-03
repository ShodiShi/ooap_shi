using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace SmartDeviceEditor
{
    public partial class MainForm : Form
    {
        // ── Данные ───────────────────────────────────────────
        private List<ISmartDevice> devices = new List<ISmartDevice>();
        private ISmartDevice selectedDevice = null;
        private static readonly Random _rnd = new Random();

        // ── Drag & Drop ──────────────────────────────────────
        private bool isDragging = false;
        private Point dragOffset;

        // ── UI элементы ──────────────────────────────────────
        private Panel panelLeft;
        private Panel panelCanvas;
        private Panel panelInfo;
        private Button btnAddCamera;
        private Button btnAddLight;
        private Button btnDuplicate;
        private Button btnDelete;
        private Label lblInfoValue;
        private ListBox listDevices;

        public MainForm()
        {
            InitializeComponent();
            BuildUI();
            SeedDevices();
        }

        // ═══════════════════════════════════════════════════
        //  ПОСТРОЕНИЕ ИНТЕРФЕЙСА
        // ═══════════════════════════════════════════════════
        private void BuildUI()
        {
            this.Text = "Smart Device Editor";
            this.Size = new Size(1100, 680);
            this.MinimumSize = new Size(1100, 680);
            this.StartPosition = FormStartPosition.CenterScreen;
            this.BackColor = Color.FromArgb(20, 22, 30);
            this.ForeColor = Color.FromArgb(220, 225, 235);
            this.Font = new Font("Segoe UI", 9f);

            // ── Левая панель ─────────────────────────────────
            panelLeft = new Panel
            {
                Dock = DockStyle.Left,
                Width = 240,
                BackColor = Color.FromArgb(26, 29, 40),
                Padding = new Padding(12)
            };

            var lblTitle = new Label
            {
                Text = "⚡ Smart Devices",
                Font = new Font("Segoe UI", 13f, FontStyle.Bold),
                ForeColor = Color.FromArgb(100, 160, 255),
                Dock = DockStyle.Top,
                Height = 44,
                TextAlign = ContentAlignment.MiddleLeft,
                Padding = new Padding(4, 0, 0, 0)
            };

            var lblDevices = new Label
            {
                Text = "УСТРОЙСТВА",
                Font = new Font("Segoe UI", 7.5f, FontStyle.Bold),
                ForeColor = Color.FromArgb(90, 100, 130),
                Dock = DockStyle.Top,
                Height = 28,
                TextAlign = ContentAlignment.BottomLeft,
                Padding = new Padding(4, 0, 0, 4)
            };

            listDevices = new ListBox
            {
                Dock = DockStyle.Top,
                Height = 180,
                BackColor = Color.FromArgb(15, 17, 24),
                ForeColor = Color.FromArgb(200, 210, 230),
                BorderStyle = BorderStyle.None,
                Font = new Font("Consolas", 9f),
                ItemHeight = 24,
                SelectionMode = SelectionMode.One
            };
            listDevices.SelectedIndexChanged += ListDevices_SelectedIndexChanged;

            btnAddCamera = CreateButton("＋  Камера", Color.FromArgb(20, 180, 140));
            btnAddLight = CreateButton("＋  Лампа", Color.FromArgb(230, 150, 40));
            btnDuplicate = CreateButton("⧉  Дублировать", Color.FromArgb(80, 120, 220));
            btnDelete = CreateButton("✕  Удалить", Color.FromArgb(200, 60, 80));

            btnAddCamera.Click += BtnAddCamera_Click;
            btnAddLight.Click += BtnAddLight_Click;
            btnDuplicate.Click += BtnDuplicate_Click;
            btnDelete.Click += BtnDelete_Click;

            // ── Инфо-блок ────────────────────────────────────
            panelInfo = new Panel
            {
                Dock = DockStyle.Bottom,
                Height = 130,
                BackColor = Color.FromArgb(15, 17, 24),
                Padding = new Padding(10)
            };

            var lblInfoTitle = new Label
            {
                Text = "ИНФОРМАЦИЯ",
                Font = new Font("Segoe UI", 7.5f, FontStyle.Bold),
                ForeColor = Color.FromArgb(90, 100, 130),
                Dock = DockStyle.Top,
                Height = 22
            };

            lblInfoValue = new Label
            {
                Text = "Выберите устройство",
                Font = new Font("Consolas", 8.5f),
                ForeColor = Color.FromArgb(160, 180, 220),
                Dock = DockStyle.Fill,
                TextAlign = ContentAlignment.TopLeft
            };

            panelInfo.Controls.Add(lblInfoValue);
            panelInfo.Controls.Add(lblInfoTitle);

            var sep = new Label
            {
                Dock = DockStyle.Top,
                Height = 1,
                BackColor = Color.FromArgb(40, 45, 60)
            };

            var lblHint = new Label
            {
                Text = "🖱 Зажми и тащи для перемещения",
                Font = new Font("Segoe UI", 7.5f),
                ForeColor = Color.FromArgb(70, 80, 110),
                Dock = DockStyle.Top,
                Height = 24,
                TextAlign = ContentAlignment.MiddleLeft,
                Padding = new Padding(4, 0, 0, 0)
            };

            panelLeft.Controls.Add(panelInfo);
            panelLeft.Controls.Add(sep);
            panelLeft.Controls.Add(lblHint);
            panelLeft.Controls.Add(btnDelete);
            panelLeft.Controls.Add(btnDuplicate);
            panelLeft.Controls.Add(btnAddLight);
            panelLeft.Controls.Add(btnAddCamera);
            panelLeft.Controls.Add(listDevices);
            panelLeft.Controls.Add(lblDevices);
            panelLeft.Controls.Add(lblTitle);

            // ── Холст ────────────────────────────────────────
            panelCanvas = new Panel
            {
                Dock = DockStyle.Fill,
                BackColor = Color.FromArgb(14, 16, 22),
                BorderStyle = BorderStyle.None
            };
            panelCanvas.Paint += PanelCanvas_Paint;
            panelCanvas.MouseDown += PanelCanvas_MouseDown;
            panelCanvas.MouseMove += PanelCanvas_MouseMove;
            panelCanvas.MouseUp += PanelCanvas_MouseUp;

            this.Controls.Add(panelCanvas);
            this.Controls.Add(panelLeft);
        }

        private Button CreateButton(string text, Color accent)
        {
            var btn = new Button
            {
                Text = text,
                Dock = DockStyle.Top,
                Height = 36,
                FlatStyle = FlatStyle.Flat,
                BackColor = Color.FromArgb(accent.R / 5, accent.G / 5, accent.B / 5),
                ForeColor = accent,
                Font = new Font("Segoe UI", 9f, FontStyle.Bold),
                Cursor = Cursors.Hand,
                TextAlign = ContentAlignment.MiddleLeft,
                Padding = new Padding(8, 0, 0, 0)
            };
            btn.FlatAppearance.BorderColor = Color.FromArgb(accent.R / 3, accent.G / 3, accent.B / 3);
            btn.FlatAppearance.BorderSize = 1;
            btn.FlatAppearance.MouseOverBackColor = Color.FromArgb(accent.R / 3, accent.G / 3, accent.B / 3);
            return btn;
        }

        // ═══════════════════════════════════════════════════
        //  ЛОГИКА
        // ═══════════════════════════════════════════════════
        private void SeedDevices()
        {
            devices.Add(new SmartCamera("1080p", 80, 80));
            devices.Add(new SmartCamera("4K", 260, 160));
            devices.Add(new SmartLight(80, 450, 100));
            RefreshList();
            panelCanvas.Invalidate();
        }

        private void RefreshList()
        {
            listDevices.Items.Clear();
            foreach (var d in devices)
                listDevices.Items.Add(d.GetInfo());
        }

        private void UpdateInfo()
        {
            lblInfoValue.Text = selectedDevice != null
                ? selectedDevice.GetInfo().Replace(" | ", "\n")
                : "Выберите устройство";
        }

        // ── Hit-test: попал ли клик в устройство ─────────
        private ISmartDevice HitTest(int x, int y)
        {
            // Идём с конца списка — верхние рисуются последними
            for (int i = devices.Count - 1; i >= 0; i--)
            {
                var device = devices[i];
                if (device is SmartCamera cam)
                {
                    if (x >= cam.X && x <= cam.X + 70 && y >= cam.Y && y <= cam.Y + 50)
                        return cam;
                }
                else if (device is SmartLight light)
                {
                    int dx = x - (light.X + 25);
                    int dy = y - (light.Y + 25);
                    if (dx * dx + dy * dy <= 25 * 25)
                        return light;
                }
            }
            return null;
        }

        // ── Mouse Down — выбор + начало drag ─────────────
        private void PanelCanvas_MouseDown(object sender, MouseEventArgs e)
        {
            if (e.Button != MouseButtons.Left) return;

            var hit = HitTest(e.X, e.Y);

            if (hit != null)
            {
                selectedDevice = hit;
                isDragging = true;

                // Смещение курсора относительно левого верхнего угла объекта
                if (hit is SmartCamera cam)
                    dragOffset = new Point(e.X - cam.X, e.Y - cam.Y);
                else if (hit is SmartLight light)
                    dragOffset = new Point(e.X - light.X, e.Y - light.Y);
            }
            else
            {
                selectedDevice = null;
                isDragging = false;
            }

            int idx = selectedDevice != null ? devices.IndexOf(selectedDevice) : -1;
            listDevices.SelectedIndexChanged -= ListDevices_SelectedIndexChanged;
            listDevices.SelectedIndex = idx;
            listDevices.SelectedIndexChanged += ListDevices_SelectedIndexChanged;

            panelCanvas.Invalidate();
            UpdateInfo();
        }

        // ── Mouse Move — перемещение ─────────────────────
        private void PanelCanvas_MouseMove(object sender, MouseEventArgs e)
        {
            if (!isDragging || selectedDevice == null) return;

            int newX = e.X - dragOffset.X;
            int newY = e.Y - dragOffset.Y;

            // Не даём вытащить за границы холста
            newX = Math.Max(0, Math.Min(newX, panelCanvas.Width - 70));
            newY = Math.Max(0, Math.Min(newY, panelCanvas.Height - 70));

            if (selectedDevice is SmartCamera cam)
            {
                cam.X = newX;
                cam.Y = newY;
            }
            else if (selectedDevice is SmartLight light)
            {
                light.X = newX;
                light.Y = newY;
            }

            panelCanvas.Invalidate();
            UpdateInfo();
        }

        // ── Mouse Up — конец drag ────────────────────────
        private void PanelCanvas_MouseUp(object sender, MouseEventArgs e)
        {
            if (isDragging)
            {
                isDragging = false;
                RefreshList(); // обновляем координаты в списке
                int idx = selectedDevice != null ? devices.IndexOf(selectedDevice) : -1;
                listDevices.SelectedIndexChanged -= ListDevices_SelectedIndexChanged;
                listDevices.SelectedIndex = idx;
                listDevices.SelectedIndexChanged += ListDevices_SelectedIndexChanged;
            }
        }

        // ── Дублирование БЕЗ паттерна Prototype ──────────
        private void BtnDuplicate_Click(object sender, EventArgs e)
        {
            if (selectedDevice == null) { Flash("Выберите устройство!"); return; }

            ISmartDevice copy = null;

            if (selectedDevice is SmartCamera origCam)
            {
                copy = new SmartCamera(origCam.Resolution, origCam.X + 90, origCam.Y + 60);
            }
            else if (selectedDevice is SmartLight origLight)
            {
                copy = new SmartLight(origLight.Brightness, origLight.X + 70, origLight.Y + 60);
            }

            if (copy != null)
            {
                devices.Add(copy);
                selectedDevice = copy;
                RefreshList();
                listDevices.SelectedIndexChanged -= ListDevices_SelectedIndexChanged;
                listDevices.SelectedIndex = devices.Count - 1;
                listDevices.SelectedIndexChanged += ListDevices_SelectedIndexChanged;
                panelCanvas.Invalidate();
                UpdateInfo();
            }
        }

        private void BtnAddCamera_Click(object sender, EventArgs e)
        {
            devices.Add(new SmartCamera("1080p", _rnd.Next(60, 600), _rnd.Next(60, 400)));
            RefreshList();
            panelCanvas.Invalidate();
        }

        private void BtnAddLight_Click(object sender, EventArgs e)
        {
            devices.Add(new SmartLight(75, _rnd.Next(60, 600), _rnd.Next(60, 400)));
            RefreshList();
            panelCanvas.Invalidate();
        }

        private void BtnDelete_Click(object sender, EventArgs e)
        {
            if (selectedDevice == null) { Flash("Выберите устройство!"); return; }
            devices.Remove(selectedDevice);
            selectedDevice = null;
            RefreshList();
            panelCanvas.Invalidate();
            UpdateInfo();
        }

        private void ListDevices_SelectedIndexChanged(object sender, EventArgs e)
        {
            int idx = listDevices.SelectedIndex;
            selectedDevice = (idx >= 0 && idx < devices.Count) ? devices[idx] : null;
            panelCanvas.Invalidate();
            UpdateInfo();
        }

        private void Flash(string msg) =>
            MessageBox.Show(msg, "Внимание", MessageBoxButtons.OK, MessageBoxIcon.Information);

        // ═══════════════════════════════════════════════════
        //  ОТРИСОВКА
        // ═══════════════════════════════════════════════════
        private void PanelCanvas_Paint(object sender, PaintEventArgs e)
        {
            var g = e.Graphics;
            g.SmoothingMode = SmoothingMode.AntiAlias;

            // Сетка
            using (var pen = new Pen(Color.FromArgb(22, 26, 36), 1))
            {
                for (int x = 0; x < panelCanvas.Width; x += 30) g.DrawLine(pen, x, 0, x, panelCanvas.Height);
                for (int y = 0; y < panelCanvas.Height; y += 30) g.DrawLine(pen, 0, y, panelCanvas.Width, y);
            }

            // Устройства
            foreach (var device in devices)
                device.Draw(g);

            // Подсветка выбранного
            if (selectedDevice is SmartCamera sc)
            {
                using (var pen = new Pen(Color.FromArgb(255, 80, 110), 2))
                {
                    pen.DashStyle = DashStyle.Dash;
                    g.DrawRectangle(pen, sc.X - 5, sc.Y - 5, 80, 60);
                }
                // Иконка перемещения
                using (var brush = new SolidBrush(Color.FromArgb(180, 255, 80, 110)))
                using (var font = new Font("Segoe UI", 8f))
                    g.DrawString("✥ drag", font, brush, sc.X, sc.Y - 18);
            }
            else if (selectedDevice is SmartLight sl)
            {
                using (var pen = new Pen(Color.FromArgb(255, 80, 110), 2))
                {
                    pen.DashStyle = DashStyle.Dash;
                    g.DrawEllipse(pen, sl.X - 5, sl.Y - 5, 60, 60);
                }
                using (var brush = new SolidBrush(Color.FromArgb(180, 255, 80, 110)))
                using (var font = new Font("Segoe UI", 8f))
                    g.DrawString("✥ drag", font, brush, sl.X, sl.Y - 18);
            }

            // Курсор меняем если наводим на объект
            var hit = HitTest(
                panelCanvas.PointToClient(Cursor.Position).X,
                panelCanvas.PointToClient(Cursor.Position).Y
            );
            panelCanvas.Cursor = hit != null ? Cursors.SizeAll : Cursors.Default;
        }
    }
}