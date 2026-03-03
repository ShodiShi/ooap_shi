using System.Drawing;

namespace SmartDeviceEditor
{
    public class SmartCamera : ISmartDevice
    {
        public string Resolution { get; set; }
        public int X { get; set; }
        public int Y { get; set; }

        public SmartCamera(string resolution, int x, int y)
        {
            Resolution = resolution; X = x; Y = y;
        }

        public void Draw(Graphics g)
        {
            // Корпус камеры
            using (var brush = new SolidBrush(System.Drawing.Color.FromArgb(18, 60, 50)))
                g.FillRectangle(brush, X, Y, 70, 50);
            using (var pen = new System.Drawing.Pen(System.Drawing.Color.FromArgb(30, 200, 140), 2))
                g.DrawRectangle(pen, X, Y, 70, 50);

            // Линза
            using (var brush = new SolidBrush(System.Drawing.Color.FromArgb(10, 30, 25)))
                g.FillEllipse(brush, X + 18, Y + 10, 30, 30);
            using (var pen = new System.Drawing.Pen(System.Drawing.Color.FromArgb(30, 200, 140), 2))
                g.DrawEllipse(pen, X + 18, Y + 10, 30, 30);
            using (var brush = new SolidBrush(System.Drawing.Color.FromArgb(20, 160, 110)))
                g.FillEllipse(brush, X + 26, Y + 18, 14, 14);

            // Текст
            using (var brush = new SolidBrush(System.Drawing.Color.FromArgb(30, 200, 140)))
            using (var font = new Font("Consolas", 7f, FontStyle.Bold))
                g.DrawString(Resolution, font, brush, X + 4, Y + 36);
        }

        public string GetInfo() =>
            $"SmartCamera | {Resolution} | X:{X} Y:{Y}";
    }
}