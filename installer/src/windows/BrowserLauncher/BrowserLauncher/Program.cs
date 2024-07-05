using System;
using System.Diagnostics;

namespace BrowserLauncher
{
    class Program
    {
        static void Main(string[] args)
        {
            string location = "http://localhost:9090/Centrifuge";
            Process browser = Process.Start(location);
        }
    }
}
