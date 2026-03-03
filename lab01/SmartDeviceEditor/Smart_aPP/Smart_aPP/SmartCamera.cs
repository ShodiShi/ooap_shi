using System.Drawing;

namespace Smart_aPP
{
    // Умная камера - конкретная реализация
    public class SmartCamera : SmartDeviceBase
    {
        private string resolution;
        private bool nightMode;
        private int angle;

        // Конструктор
        public SmartCamera(int x, int y, string resolution = "1080p", bool nightMode = false, int angle = 90)
            : base(x, y)
        {
            this.resolution = resolution;
            this.nightMode = nightMode;
            this.angle = angle;
        }

        // Свойства
        public string Resolution
        {
            get { return resolution; }
            set { resolution = value; }
        }

        public bool NightMode
        {
            get { return nightMode; }
            set { nightMode = value; }
        }

        public int Angle
        {
            get { return angle; }
            set { angle = value; }
        }

        // ПАТТЕРН PROTOTYPE - клонирование
        public override ISmartDevice Clone()
        {
            var clone = new SmartCamera(this.x + 20, this.y + 20);
            clone.resolution = this.resolution;
            clone.nightMode = this.nightMode;
            clone.angle = this.angle;
            return clone;
        }

        // Рисование камеры
        public override void Draw(Graphics g)
        {
            // Корпус камеры (синий квадрат)
            using (SolidBrush brush = new SolidBrush(Color.SteelBlue))
            {
                g.FillRectangle(brush, x, y, DeviceSize, DeviceSize);
            }

            // Обводка
            g.DrawRectangle(Pens.Black, x, y, DeviceSize, DeviceSize);

            // Объектив (чёрный круг)
            using (SolidBrush lensBrush = new SolidBrush(Color.Black))
            {
                int lensSize = 20;
                int lensX = x + (DeviceSize - lensSize) / 2;
                int lensY = y + (DeviceSize - lensSize) / 2;
                g.FillEllipse(lensBrush, lensX, lensY, lensSize, lensSize);
            }

            // Индикатор ночного режима
            if (nightMode)
            {
                using (SolidBrush ledBrush = new SolidBrush(Color.Red))
                {
                    g.FillEllipse(ledBrush, x + 5, y + 5, 8, 8);
                }
            }

            // Подпись
            using (Font font = new Font("Arial", 8))
            using (SolidBrush textBrush = new SolidBrush(Color.White))
            {
                g.DrawString("CAM", font, textBrush, x + 10, y + 35);
            }
        }

        // Информация об устройстве
        public override string GetInfo()
        {
            return string.Format("SmartCamera\nПозиция: ({0}, {1})\nРазрешение: {2}\nНочной режим: {3}\nУгол: {4}°",
                x, y, resolution, nightMode ? "ВКЛ" : "ВЫКЛ", angle);
        }
    }
}