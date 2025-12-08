#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLEAdvertising.h>

#define CHARACTERISTIC_UUID "70752306-a96c-4234-9dfc-7190be82281e"

constexpr std::string_view deviceName = "InNav Tag";
const char manufacturerId[] = { 0xFF, 0xFF };
const char manufacturerData[] = { 0x03, 0x00, 0x00, 0x00 };

void setup() {
  BLEDevice::init(std::string(deviceName));

  esp_ble_tx_power_set(ESP_BLE_PWR_TYPE_ADV, ESP_PWR_LVL_P15);

  BLEServer* pServer = BLEDevice::createServer();

  BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinInterval(160);
  pAdvertising->setMaxInterval(160);
  pAdvertising->setAdvertisementType(ADV_TYPE_NONCONN_IND);

  BLEAdvertisementData advData;
  const auto advertisedData = std::string(manufacturerId, sizeof(manufacturerId)) 
                            + std::string(manufacturerData, sizeof(manufacturerData));
  advData.setManufacturerData(advertisedData);
  advData.setName(std::string(deviceName));
  
  pAdvertising->setAdvertisementData(advData);

  BLEDevice::startAdvertising();
}

void loop() {}
