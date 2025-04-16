
import 'package:flutter/material.dart';

class MeasurementWidget extends StatelessWidget {
   MeasurementWidget({super.key});

  bool isMeasureStart=false,isDeviceConnected=false;


  @override
  Widget build(BuildContext context) {
    return  Column(

      children: [

        if(isMeasureStart)
          Text("Connection status:${ isDeviceConnected ?"Connected" :"Disconnected"}"),



        if(isDeviceConnected)
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [

              const SizedBox(
                  height: 30,
                  width: 30,
                  child: CircularProgressIndicator(color: Colors.white,)),

              SizedBox(width: 20,),

              Text("Measuring...  please wait",),
            ],
          )  else

          Column(
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [

                  Text("Searching Device...",),

                  // UiHelper.horizontalSpaceSmall,
                  // SpinKitThreeBounce(color: AppColors.white,size: 25,)



                ],),
              // UiHelper.verticalSpaceSmall,
              Text("Please  run the  device then press 'Start Measurement' 👆 ",),

            ],
          )
        ,





      ],

    );
  }
}
