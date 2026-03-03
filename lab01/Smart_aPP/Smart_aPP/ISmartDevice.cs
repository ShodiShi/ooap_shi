using System.Drawing;

namespace Smart_aPP
{
    public interface ISmartDevice
    {
        // Паттерн Prototype - клонирование устройства
        ISmartDevice Clone();

        // Рисование устройства на холсте
        void Draw(Graphics g);

        // Получение информации об устройстве
        string GetInfo();

        // Проверка попадания точки в устройство
        bool ContainsPoint(int x, int y);

        // Свойства координат
        int X { get; set; }
        int Y { get; set; }
    }
}