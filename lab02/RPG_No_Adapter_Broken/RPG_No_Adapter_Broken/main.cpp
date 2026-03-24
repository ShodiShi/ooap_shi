#include <iostream>
#include <memory>
#include "IEnemy.h"
#include "IWeapon.h"
#include "LegacyMonster.h"
#include "OldWeapon.h"

int main() {
    // 1. Ошибка типизации (Несовместимость классов)
    // Мы пытаемся положить OldWeapon в указатель IWeapon. 
    IWeapon* weapon = new OldWeapon();

    // 2. Ошибка вызова (Несовместимость методов)
    LegacyMonster* monster = new LegacyMonster("Orc", 100);

    // Мы хотим нанести урон, как привыкли в новой игре:
    monster->TakeDamage(15);

    return 0;
}