using System.Drawing;

namespace Smart_aPP
{
    public class SmartLight : SmartDeviceBase
    {
        private int brightness;
        private int colorTemp;
        private bool state;

        public SmartLight(int x, int y, int brightness = 100, int colorTemp = 4000, bool state = true)
            : base(x, y)
        {
            this.brightness = brightness;
            this.colorTemp = colorTemp;
            this.state = state;
        }

        public int Brightness
        {
            get { return brightness; }
            set { brightness = value; }
        }

        public int ColorTemp
        {
            get { return colorTemp; }
            set { colorTemp = value; }
        }

        public bool State
        {
            get { return state; }
            set { state = value; }
        }

        public override ISmartDevice Clone()
        {
            var clone = new SmartLight(this.x + 20, this.y + 20);
            clone.brightness = this.brightness;
            clone.colorTemp = this.colorTemp;
            clone.state = this.state;
            return clone;
        }

        public override void Draw(Graphics g)
        {
            Color bulbColor;
            if (!state)
            {
                bulbColor = Color.Gray;
            }
            else
            {
                int r = 255;
                int green = 255 - (6500 - colorTemp) / 40;
                int b = colorTemp > 4000 ? 255 : 150;

                r = r * brightness / 100;
                green = green * brightness / 100;
                b = b * brightness / 100;

                bulbColor = Color.FromArgb(r, green, b);
            }

            using (SolidBrush brush = new SolidBrush(bulbColor))
            {
                g.FillEllipse(brush, x, y, DeviceSize, DeviceSize);
            }

            g.DrawEllipse(Pens.Black, x, y, DeviceSize, DeviceSize);

            using (SolidBrush baseBrush = new SolidBrush(Color.Silver))
            {
                Rectangle baseRect = new Rectangle(x + 15, y + 40, 20, 10);
                g.FillRectangle(baseBrush, baseRect);
                g.DrawRectangle(Pens.Black, baseRect);
            }

            if (state)
            {
                using (Pen rayPen = new Pen(Color.FromArgb(100, 255, 255, 0), 2))
                {
                    int centerX = x + DeviceSize / 2;
                    int centerY = y + DeviceSize / 2;

                    for (int i = 0; i < 8; i++)
                    {
                        double angle = i * System.Math.PI / 4;
                        int x1 = centerX + (int)(System.Math.Cos(angle) * DeviceSize / 2);
                        int y1 = centerY + (int)(System.Math.Sin(angle) * DeviceSize / 2);
                        int x2 = centerX + (int)(System.Math.Cos(angle) * 35);
                        int y2 = centerY + (int)(System.Math.Sin(angle) * 35);
                        g.DrawLine(rayPen, x1, y1, x2, y2);
                    }
                }
            }

            using (Font font = new Font("Arial", 7))
            using (SolidBrush textBrush = new SolidBrush(Color.Black))
            {
                g.DrawString("LIGHT", font, textBrush, x + 8, y - 12);
            }
        }

        public override string GetInfo()
        {
            return string.Format("SmartLight\nПозиция: ({0}, {1})\nСостояние: {2}\nЯркость: {3}%\nТемпература: {4}K",
                x, y, state ? "ВКЛ" : "ВЫКЛ", brightness, colorTemp);
        }
    }
}