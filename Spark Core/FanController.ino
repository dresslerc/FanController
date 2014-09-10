const int io_dip1 = 0;
const int io_dip2 = 1;
const int io_dip3 = 2;
const int io_fanoff = 3;
const int io_fanlo = 4;
const int io_fanmed = 5;
const int io_fanhigh = 6;

int fanStat[7];

void setup() {
    
    for (int i = 0; i < 7; i++)  {
        fanStat[i] = -1;
    }
        
    // Controls onboard RGB led
    RGB.control(true);
    RGB.color(0, 0, 30);
    
    // Sets up Pins Modes
    pinMode(io_dip1, OUTPUT);
    pinMode(io_dip2, OUTPUT);
    pinMode(io_dip3, OUTPUT);
    pinMode(io_fanoff, OUTPUT);
    pinMode(io_fanlo, OUTPUT);
    pinMode(io_fanmed, OUTPUT);
    pinMode(io_fanhigh, OUTPUT);
  
    // Sets Cloud function calls
    Spark.function("fancontrol", fanControl);
    Spark.function("fanstatus", fanStatus);
    
    //Serial.begin(9600);
    
}

void loop() {

}

int fanStatus(String command)
{
    int address = command.toInt();;
    
    // Memory
    return fanStat[address];
    
    // EEPROM
    //return SparkFlash_read(address);
}

int fanControl(String command) 
{
   RGB.color(0, 30, 0);
   int address = setDipSwitches(command.substring(0,1));
   delay(200);
   
   // Memory
   fanStat[address] = setFanMode(command.substring(1));
   
   // EEPROM
   //SparkFlash_write(address * 2,  setFanMode(command.substring(1)));
   
   delay(1000);
   gpioReset();
   RGB.color(0, 0, 30);
   
   // Memory
   return fanStat[address];
   
   // EEPROM
   //return SparkFlash_read(address);
}

int setDipSwitches(String address) {
    
    if (address == "0") {
        digitalWrite(io_dip1, LOW);
        digitalWrite(io_dip2, LOW);
        digitalWrite(io_dip3, LOW);
        
        return 0;
    }
    
    if (address == "1") {
        digitalWrite(io_dip1, LOW);
        digitalWrite(io_dip2, LOW);
        digitalWrite(io_dip3, HIGH);
        
        return 1;
    }

    if (address == "2") {
        digitalWrite(io_dip1, LOW);
        digitalWrite(io_dip2, HIGH);
        digitalWrite(io_dip3, LOW);
        
        return 2;
    }
    
    if (address == "3") {
        digitalWrite(io_dip1, LOW);
        digitalWrite(io_dip2, HIGH);
        digitalWrite(io_dip3, HIGH);
        
        return 3;
    }
    
    if (address == "4") {
        digitalWrite(io_dip1, HIGH);
        digitalWrite(io_dip2, LOW);
        digitalWrite(io_dip3, LOW);
        
        return 4;
    }            
   
    if (address == "5") {
        digitalWrite(io_dip1, HIGH);
        digitalWrite(io_dip2, LOW);
        digitalWrite(io_dip3, HIGH);
        
        return 5;
    }     

    if (address == "6") {
        digitalWrite(io_dip1, HIGH);
        digitalWrite(io_dip2, HIGH);
        digitalWrite(io_dip3, LOW);
        
        return 6;
    }      
   
    if (address == "7") {
        digitalWrite(io_dip1, HIGH);
        digitalWrite(io_dip2, HIGH);
        digitalWrite(io_dip3, HIGH);
        
        return 7;
    } 
    
    return -1;
}

int setFanMode(String command) {
    
    //Serial.println(command);
    
    if (command == "of") {
        digitalWrite(io_fanoff, HIGH);
        
        return 0;
    }
    
    if (command == "lo") {
        digitalWrite(io_fanlo, HIGH);
        
        return 1;
    }
    
    if (command == "me") {
        digitalWrite(io_fanmed, HIGH);
        
        return 2;
    }
    
    if (command == "hi") {
        digitalWrite(io_fanhigh, HIGH);
        
        return 3;
    }
    
    return -1;
}

void gpioReset() {
    
    digitalWrite(io_dip1, LOW);
    digitalWrite(io_dip2, LOW);
    digitalWrite(io_dip3, LOW);
    digitalWrite(io_fanoff, LOW);
    digitalWrite(io_fanlo, LOW);
    digitalWrite(io_fanmed, LOW);
    digitalWrite(io_fanhigh, LOW);
    
}

int SparkFlash_read(int address)
{
  if (address & 1)
    return -1; // error, can only access half words

  uint8_t values[2];
  sFLASH_ReadBuffer(values, 0x80000 + address, 2);
  return (values[0] << 8) | values[1];
}

int SparkFlash_write(int address, uint16_t value)
{
  if (address & 1)
    return -1; // error, can only access half words

  uint8_t values[2] = {
    (uint8_t)((value >> 8) & 0xff),
    (uint8_t)(value & 0xff)
  };
  sFLASH_WriteBuffer(values, 0x80000 + address, 2);
  return 2; // or anything else signifying it worked
}

