using System.Drawing;

namespace SmartDeviceEditor
{
    public interface ISmartDevice
    {
        void Draw(Graphics g);
        string GetInfo();
    }
}