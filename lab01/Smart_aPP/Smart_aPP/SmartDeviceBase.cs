using System.Drawing;

namespace Smart_aPP
{
    // Абстрактный базовый класс для всех умных устройств
    // Содержит общие координаты x, y
    public abstract class SmartDeviceBase : ISmartDevice
    {
        // Protected - доступны наследникам для Clone()
        protected int x;
        protected int y;

        // Размер устройства для отображения
        protected const int DeviceSize = 50;

        // Конструктор
        protected SmartDeviceBase(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        // Свойства для доступа к координатам
        public int X
        {
            get { return x; }
            set { x = value; }
        }

        public int Y
        {
            get { return y; }
            set { y = value; }
        }

        // Абстрактные методы - реализуют наследники
        public abstract ISmartDevice Clone();
        public abstract void Draw(Graphics g);
        public abstract string GetInfo();

        // Общая реализация для всех устройств
        public virtual bool ContainsPoint(int px, int py)
        {
            return px >= x && px <= x + DeviceSize &&
                   py >= y && py <= y + DeviceSize;
        }
    }
}