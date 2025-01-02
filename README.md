# sm_lepu

This plugin to communicate with some of lepu devices  
Now it support **Aoj-20a** for temperature and **PC60FW**  for spo2
## Getting Started


To start use this plugin import it like :



Then you can check permission like:

    _smLepuPlugin.checkPermission();

Now you can use the device that you need  like :

for temperature:


                   _smLepuPlugin.getEvents().listen((onData){


                  result=onData;
                  setState(() {

                  });

                });

                await  _smLepuPlugin.readTemp();


for spo2:
          
    _smLepuPlugin.getEvents().listen((onData){


                  result=onData;
                  setState(() {

                  });

                });

                await  _smLepuPlugin.readSpo2();



See the example for more understanding

I hope this clear
