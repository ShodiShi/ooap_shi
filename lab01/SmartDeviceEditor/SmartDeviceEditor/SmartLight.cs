using System.Drawing;

namespace SmartDeviceEditor
{
    public class SmartLight : ISmartDevice
    {
        public int Brightness { get; set; }
        public int X { get; set; }
        public int Y { get; set; }

        public SmartLight(int brightness, int x, int y)
        {
            Brightness = brightness; X = x; Y = y;
        }

        public void Draw(Graphics g)
        {
            // Свечение
            using (var brush = new SolidBrush(System.Drawing.Color.FromArgb(
                (int)(Brightness * 0.6), 80, 40, 0)))
                g.FillEllipse(brush, X - 10, Y - 10, 70, 70);

            // Лампа
            using (var brush = new SolidBrush(System.Drawing.Color.FromArgb(60, 35, 5)))
                g.FillEllipse(brush, X, Y, 50, 50);
            using (var pen = new System.Drawing.Pen(System.Drawing.Color.FromArgb(230, 150, 40), 2))
                g.DrawEllipse(pen, X, Y, 50, 50);

            // Яркость текст
            using (var brush = new SolidBrush(System.Drawing.Color.FromArgb(230, 150, 40)))
            using (var font = new Font("Consolas", 8f, FontStyle.Bold))
                g.DrawString($"{Brightness}%", font, brush, X + 10, Y + 18);
        }

        public string GetInfo() =>
            $"SmartLight | {Brightness}% | X:{X} Y:{Y}";
    }
}