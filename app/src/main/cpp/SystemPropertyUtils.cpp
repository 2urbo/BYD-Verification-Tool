#include <string>
#include <vector>
#include <sys/system_properties.h>

std::string getSystemProperty(const std::string& propertyName) {
    char propertyValue[PROP_VALUE_MAX];
    if (__system_property_get(propertyName.c_str(), propertyValue)) {
        return {propertyValue};
    }
    return {};
}