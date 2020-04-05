// Variables needed for calculation
long _prev_millis_value, _current_millis_value, _current_consumption, _last_out, _last_calc;
bool _last_state, _current_state;

// Constants
long _pulses_per_kilowatt_hour = 14300;
int _voltage = 240;
int _pin = 9;

void setup() {
  // Pulse pin needs to be pullup internally, since there is no pullup on the board
  pinMode( _pin, INPUT_PULLUP );
  Serial.begin( 9600 );
}

void loop() {
  calcConsumption();
  Serial.println( _current_consumption  );
}

void calcConsumption() {
  // Set last state to current state and re-read current state
  _last_state = _current_state;
  _current_state = digitalRead( _pin );
  
  // Rising edge on signal, re-calc current consumption
  if (_current_state == LOW && _last_state == HIGH) {
    
    // Set prev millis to current millis and re-set current millis
    _prev_millis_value = _current_millis_value;
    _current_millis_value = millis();
  
    // Since 1 Wh = 3600 Ws, 1 kWh will equal 3600000 Ws
    long pulses_per_watt_second = 3600000 / _pulses_per_kilowatt_hour;
  
    // Delta of time in seconds
    float deltaT = ( _current_millis_value - _prev_millis_value ) / ( float ) 1000;
    
    _last_calc = millis();
    _current_consumption = ( int ) pulses_per_watt_second / ( deltaT * _voltage );

    //Serial.println( 1 );
  }
  
  // Just set 0 when no pulses occur, also keep a 400ms threshold for doing that
  // because the signal can get quite slow at low watt amounts
  else if( millis() - _last_calc > 400 ) {
    _current_consumption = 0;
    //Serial.println( 2 );
  }
}
