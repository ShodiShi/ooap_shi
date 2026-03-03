using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace Smart_aPP
{
    public partial class Form1 : Form
    {
        private List<ISmartDevice> devices;
        private ISmartDevice selectedDevice;
        private ISmartDevice draggedDevice;
        private Point dragOffset;

        private Panel canvas;
        private Button btnDuplicate;
        private Button btnAddCamera;
        private Button btnAddLight;
        private Button btnClear;
        private Label lblInfo;
        private Label lblCount;

        private static readonly Random _rnd = new Random();

        public Form1()
        {
            InitializeComponent();
            InitializeUI();

            devices = new List<ISmartDevice>();
            selectedDevice = null;
            draggedDevice = null;

            devices.Add(new SmartCamera(100, 100, "1080p", false, 90));
            devices.Add(new SmartLight(250, 100, 80, 4000, true));
            devices.Add(new SmartCamera(100, 250, "4K", true, 120));
        }

        private void InitializeUI()
        {
            // ── Форма ────────────────────────────────────────
            this.Text = "Smart Device Manager";
            this.Size = new Size(1100, 720);
            this.MinimumSize = new Size(1100, 720);
            this.StartPosition = FormStartPosition.CenterScreen;
            this.BackColor = Color.FromArgb(13, 15, 20);
            this.ForeColor = Color.FromArgb(220, 225, 240);
            this.Font = new Font("Segoe UI", 9f);

            // ── Топ-бар ──────────────────────────────────────
            Panel topBar = new Panel();
            topBar.Dock = DockStyle.Top;
            topBar.Height = 52;
            topBar.BackColor = Color.FromArgb(18, 21, 30);
            this.Controls.Add(topBar);

            Label lblAppTitle = new Label();
            lblAppTitle.Text = "⚡  SMART DEVICE MANAGER";
            lblAppTitle.Font = new Font("Segoe UI", 13f, FontStyle.Bold);
            lblAppTitle.ForeColor = Color.FromArgb(80, 140, 255);
            lblAppTitle.Location = new Point(20, 0);
            lblAppTitle.Size = new Size(400, 52);
            lblAppTitle.TextAlign = ContentAlignment.MiddleLeft;
            topBar.Controls.Add(lblAppTitle);

            Label lblSub = new Label();
            lblSub.Text = "Prototype Pattern  •  Drag & Drop Canvas";
            lblSub.Font = new Font("Segoe UI", 8.5f);
            lblSub.ForeColor = Color.FromArgb(70, 80, 110);
            lblSub.Location = new Point(420, 0);
            lblSub.Size = new Size(400, 52);
            lblSub.TextAlign = ContentAlignment.MiddleLeft;
            topBar.Controls.Add(lblSub);

            // ── Разделитель под топ-баром ────────────────────
            Panel topLine = new Panel();
            topLine.Dock = DockStyle.Top;
            topLine.Height = 1;
            topLine.BackColor = Color.FromArgb(30, 35, 55);
            this.Controls.Add(topLine);

            // ── Правая панель ────────────────────────────────
            Panel rightPanel = new Panel();
            rightPanel.Dock = DockStyle.Right;
            rightPanel.Width = 260;
            rightPanel.BackColor = Color.FromArgb(16, 18, 26);
            this.Controls.Add(rightPanel);

            // Левая граница правой панели
            Panel rightBorder = new Panel();
            rightBorder.Dock = DockStyle.Left;
            rightBorder.Width = 1;
            rightBorder.BackColor = Color.FromArgb(30, 35, 55);
            rightPanel.Controls.Add(rightBorder);

            // Внутренний контейнер с отступами
            Panel innerRight = new Panel();
            innerRight.Dock = DockStyle.Fill;
            innerRight.Padding = new Padding(16, 20, 16, 16);
            innerRight.BackColor = Color.Transparent;
            rightPanel.Controls.Add(innerRight);

            // Секция: заголовок
            Label lblPanelTitle = new Label();
            lblPanelTitle.Text = "УПРАВЛЕНИЕ";
            lblPanelTitle.Font = new Font("Segoe UI", 7.5f, FontStyle.Bold);
            lblPanelTitle.ForeColor = Color.FromArgb(60, 70, 100);
            lblPanelTitle.Dock = DockStyle.Top;
            lblPanelTitle.Height = 24;
            lblPanelTitle.TextAlign = ContentAlignment.BottomLeft;
            innerRight.Controls.Add(lblPanelTitle);

            // Счётчик
            lblCount = new Label();
            lblCount.Text = "Устройств: 3";
            lblCount.Dock = DockStyle.Top;
            lblCount.Height = 38;
            lblCount.Font = new Font("Segoe UI", 11f, FontStyle.Bold);
            lblCount.ForeColor = Color.FromArgb(80, 140, 255);
            lblCount.BackColor = Color.FromArgb(20, 40, 80, 255);
            lblCount.TextAlign = ContentAlignment.MiddleCenter;
            innerRight.Controls.Add(lblCount);

            var spacer0 = MakeSpacer(8);
            innerRight.Controls.Add(spacer0);

            // Кнопки
            btnAddCamera = MakeButton("＋  Добавить камеру", Color.FromArgb(20, 160, 120));
            btnAddCamera.Click += BtnAddCamera_Click;
            innerRight.Controls.Add(btnAddCamera);

            var spacer1 = MakeSpacer(6);
            innerRight.Controls.Add(spacer1);

            btnAddLight = MakeButton("＋  Добавить лампу", Color.FromArgb(210, 140, 20));
            btnAddLight.Click += BtnAddLight_Click;
            innerRight.Controls.Add(btnAddLight);

            var spacer2 = MakeSpacer(6);
            innerRight.Controls.Add(spacer2);

            btnDuplicate = MakeButton("⧉  Дублировать", Color.FromArgb(60, 100, 210));
            btnDuplicate.Enabled = false;
            btnDuplicate.Click += BtnDuplicate_Click;
            innerRight.Controls.Add(btnDuplicate);

            var spacer3 = MakeSpacer(6);
            innerRight.Controls.Add(spacer3);

            btnClear = MakeButton("✕  Очистить всё", Color.FromArgb(180, 45, 60));
            btnClear.Click += BtnClear_Click;
            innerRight.Controls.Add(btnClear);

            // Разделитель
            var divider = MakeSpacer(1);
            divider.BackColor = Color.FromArgb(30, 35, 55);
            divider.Dock = DockStyle.Top;
            divider.Height = 1;
            innerRight.Controls.Add(divider);

            var spacer4 = MakeSpacer(14);
            innerRight.Controls.Add(spacer4);

            // Заголовок инфо
            Label lblInfoTitle = new Label();
            lblInfoTitle.Text = "ИНФОРМАЦИЯ";
            lblInfoTitle.Font = new Font("Segoe UI", 7.5f, FontStyle.Bold);
            lblInfoTitle.ForeColor = Color.FromArgb(60, 70, 100);
            lblInfoTitle.Dock = DockStyle.Top;
            lblInfoTitle.Height = 20;
            lblInfoTitle.TextAlign = ContentAlignment.BottomLeft;
            innerRight.Controls.Add(lblInfoTitle);

            var spacer5 = MakeSpacer(6);
            innerRight.Controls.Add(spacer5);

            // Инфо-блок
            lblInfo = new Label();
            lblInfo.Text = "Выберите устройство\nна холсте";
            lblInfo.Dock = DockStyle.Top;
            lblInfo.Height = 160;
            lblInfo.Font = new Font("Consolas", 8.5f);
            lblInfo.ForeColor = Color.FromArgb(150, 170, 220);
            lblInfo.BackColor = Color.FromArgb(10, 12, 18);
            lblInfo.Padding = new Padding(10, 10, 10, 10);
            lblInfo.BorderStyle = BorderStyle.None;
            innerRight.Controls.Add(lblInfo);

            var spacer6 = MakeSpacer(12);
            innerRight.Controls.Add(spacer6);

            // Подсказка
            Label lblHint = new Label();
            lblHint.Text = "🖱  Зажми и тащи для\n      перемещения";
            lblHint.Dock = DockStyle.Top;
            lblHint.Height = 40;
            lblHint.Font = new Font("Segoe UI", 8f, FontStyle.Italic);
            lblHint.ForeColor = Color.FromArgb(50, 60, 90);
            lblHint.TextAlign = ContentAlignment.MiddleLeft;
            innerRight.Controls.Add(lblHint);

            // ── Холст ────────────────────────────────────────
            canvas = new Panel();
            canvas.Dock = DockStyle.Fill;
            canvas.BackColor = Color.FromArgb(11, 13, 18);
            canvas.Paint += Canvas_Paint;
            canvas.MouseDown += Canvas_MouseDown;
            canvas.MouseMove += Canvas_MouseMove;
            canvas.MouseUp += Canvas_MouseUp;
            canvas.Cursor = Cursors.Default;
            this.Controls.Add(canvas);
        }

        // ── Фабрика кнопок ───────────────────────────────────
        private Button MakeButton(string text, Color accent)
        {
            var dimBg = Color.FromArgb(accent.R / 6, accent.G / 6, accent.B / 6);
            var hoverBg = Color.FromArgb(accent.R / 3, accent.G / 3, accent.B / 3);

            Button btn = new Button();
            btn.Text = text;
            btn.Dock = DockStyle.Top;
            btn.Height = 40;
            btn.FlatStyle = FlatStyle.Flat;
            btn.BackColor = dimBg;
            btn.ForeColor = accent;
            btn.Font = new Font("Segoe UI", 9.5f, FontStyle.Bold);
            btn.Cursor = Cursors.Hand;
            btn.TextAlign = ContentAlignment.MiddleLeft;
            btn.Padding = new Padding(10, 0, 0, 0);
            btn.FlatAppearance.BorderColor = Color.FromArgb(accent.R / 4, accent.G / 4, accent.B / 4);
            btn.FlatAppearance.BorderSize = 1;
            btn.FlatAppearance.MouseOverBackColor = hoverBg;
            return btn;
        }

        private Panel MakeSpacer(int height)
        {
            return new Panel
            {
                Dock = DockStyle.Top,
                Height = height,
                BackColor = Color.Transparent
            };
        }

        // ═══════════════════════════════════════════════════
        //  ЛОГИКА — НЕ ИЗМЕНЕНА
        // ═══════════════════════════════════════════════════
        private void BtnAddCamera_Click(object sender, EventArgs e)
        {
            int x = _rnd.Next(50, 600);
            int y = _rnd.Next(50, 500);
            devices.Add(new SmartCamera(x, y));
            UpdateCount();
            canvas.Invalidate();
        }

        private void BtnAddLight_Click(object sender, EventArgs e)
        {
            int x = _rnd.Next(50, 600);
            int y = _rnd.Next(50, 500);
            devices.Add(new SmartLight(x, y));
            UpdateCount();
            canvas.Invalidate();
        }

        private void BtnDuplicate_Click(object sender, EventArgs e)
        {
            if (selectedDevice != null)
            {
                ISmartDevice clone = selectedDevice.Clone();
                devices.Add(clone);
                lblInfo.Text = "✅ Клонировано!\n\n" + clone.GetInfo();
                UpdateCount();
                canvas.Invalidate();
            }
        }

        private void BtnClear_Click(object sender, EventArgs e)
        {
            devices.Clear();
            selectedDevice = null;
            lblInfo.Text = "Все устройства удалены";
            btnDuplicate.Enabled = false;
            UpdateCount();
            canvas.Invalidate();
        }

        private void UpdateCount()
        {
            lblCount.Text = "Устройств: " + devices.Count;
        }

        private void Canvas_MouseDown(object sender, MouseEventArgs e)
        {
            foreach (var device in devices)
            {
                if (device.ContainsPoint(e.X, e.Y))
                {
                    selectedDevice = device;
                    draggedDevice = device;
                    dragOffset = new Point(e.X - device.X, e.Y - device.Y);
                    lblInfo.Text = device.GetInfo();
                    btnDuplicate.Enabled = true;
                    canvas.Invalidate();
                    return;
                }
            }
            selectedDevice = null;
            draggedDevice = null;
            btnDuplicate.Enabled = false;
            canvas.Invalidate();
        }

        private void Canvas_MouseMove(object sender, MouseEventArgs e)
        {
            if (draggedDevice != null && e.Button == MouseButtons.Left)
            {
                draggedDevice.X = e.X - dragOffset.X;
                draggedDevice.Y = e.Y - dragOffset.Y;
                lblInfo.Text = draggedDevice.GetInfo();
                canvas.Invalidate();
            }

            // Меняем курсор при наведении на объект
            bool overDevice = false;
            foreach (var device in devices)
                if (device.ContainsPoint(e.X, e.Y)) { overDevice = true; break; }
            canvas.Cursor = overDevice ? Cursors.SizeAll : Cursors.Default;
        }

        private void Canvas_MouseUp(object sender, MouseEventArgs e)
        {
            draggedDevice = null;
        }

        // ═══════════════════════════════════════════════════
        //  ОТРИСОВКА
        // ═══════════════════════════════════════════════════
        private void Canvas_Paint(object sender, PaintEventArgs e)
        {
            Graphics g = e.Graphics;
            g.SmoothingMode = SmoothingMode.AntiAlias;

            // Точечная сетка
            using (SolidBrush dotBrush = new SolidBrush(Color.FromArgb(35, 40, 60)))
            {
                for (int x = 0; x < canvas.Width; x += 40)
                    for (int y = 0; y < canvas.Height; y += 40)
                        g.FillEllipse(dotBrush, x - 1, y - 1, 2, 2);
            }

            // Устройства
            foreach (var device in devices)
            {
                device.Draw(g);

                // Подсветка выбранного — пунктирная рамка
                if (device == selectedDevice)
                {
                    using (Pen selPen = new Pen(Color.FromArgb(80, 140, 255), 2))
                    {
                        selPen.DashStyle = DashStyle.Dash;
                        g.DrawRectangle(selPen, device.X - 7, device.Y - 7, 64, 64);
                    }
                    // Угловые акценты
                    int ax = device.X - 7, ay = device.Y - 7;
                    using (Pen corner = new Pen(Color.FromArgb(80, 140, 255), 2))
                    {
                        g.DrawLine(corner, ax, ay, ax + 10, ay);
                        g.DrawLine(corner, ax, ay, ax, ay + 10);
                        g.DrawLine(corner, ax + 64, ay, ax + 54, ay);
                        g.DrawLine(corner, ax + 64, ay, ax + 64, ay + 10);
                        g.DrawLine(corner, ax, ay + 64, ax + 10, ay + 64);
                        g.DrawLine(corner, ax, ay + 64, ax, ay + 54);
                        g.DrawLine(corner, ax + 64, ay + 64, ax + 54, ay + 64);
                        g.DrawLine(corner, ax + 64, ay + 64, ax + 64, ay + 54);
                    }
                }
            }
        }
    }
}