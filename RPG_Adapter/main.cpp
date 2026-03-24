#include <SFML/Graphics.hpp>
#include <vector>
#include <string>
#include <cmath>
#include <cstdlib>
#include <ctime>
#include <memory>
#include <iostream>

#include "BattleArena.h"
#include "ModernBow.h"
#include "WeaponAdapter.h"
#include "OldWeapon.h"
#include "MonsterAdapter.h"
#include "LegacyMonster.h"

const int TILE_SIZE = 16;
const float SCALE = 3.0f;
const int VIEWPORT_W = 20;
const int VIEWPORT_H = 15;

enum GameState { PROLOGUE, PLAYING, BATTLE };
GameState currentState = PROLOGUE;

struct Entity {
    float x = 0, y = 0;
    float vx = 0, vy = 0;
    float targetX = 0, targetY = 0;
    int hp = 100, maxHp = 100;
    std::string name = "";
    std::shared_ptr<sf::Sprite> sprite;
    bool alive = true;
    float animTimer = 0;
    bool facingRight = true;
};

struct WeaponPickup {
    float x = 0, y = 0;
    bool isModern = false;
    bool pickedUp = false;
    std::string name = "";
    std::shared_ptr<sf::Sprite> sprite;
    float rotationAngle = 0;
};

struct Animal {
    float x = 0, y = 0;
    float vx = 0, vy = 0;
    std::string type = "";
    std::shared_ptr<sf::Sprite> sprite;
    float animTimer = 0;
    float wanderTimer = 0;
};

int getTileAt(int x, int y)
{
    int seed = x * 73856093 ^ y * 19349663;
    int random = (seed & 0x7FFFFFFF) % 100;

    if ((x == 10 && y == 8) || (x == 30 && y == 8) || (x == 13 && y == 23))
        return 5;
    if ((x >= 7 && x <= 12 && y == 9) || (x >= 7 && x <= 12 && y == 10) ||
        (x >= 27 && x <= 32 && y == 9) || (x >= 27 && x <= 32 && y == 10) ||
        (x >= 10 && x <= 17 && y == 24) || (x >= 10 && x <= 17 && y == 25))
        return 4;

    if (random < 5) return 3;
    if (random < 12) return 1;
    if (random < 18) return 2;
    return 0;
}

void drawHealthBar(sf::RenderWindow& window, float x, float y, int hp, int maxHp)
{
    sf::RectangleShape bg({ 40, 5 });
    bg.setPosition({ x - 20, y - 30 });
    bg.setFillColor(sf::Color(50, 50, 50));
    window.draw(bg);

    float hpPercent = (float)hp / maxHp;
    sf::RectangleShape bar({ 40 * hpPercent, 5 });
    bar.setPosition({ x - 20, y - 30 });

    if (hpPercent > 0.6f)
        bar.setFillColor(sf::Color(50, 255, 50));
    else if (hpPercent > 0.3f)
        bar.setFillColor(sf::Color(255, 255, 50));
    else
        bar.setFillColor(sf::Color(255, 50, 50));

    window.draw(bar);
}

void drawNameTag(sf::RenderWindow& window, sf::Font& font, float x, float y, const std::string& name, sf::Color color)
{
    sf::Text text(font);
    text.setString(name);
    text.setCharacterSize(10);
    text.setFillColor(color);
    text.setOutlineColor(sf::Color::Black);
    text.setOutlineThickness(1);

    sf::FloatRect bounds = text.getLocalBounds();
    text.setOrigin({ bounds.size.x / 2, bounds.size.y / 2 });
    text.setPosition({ x, y - 40 });

    window.draw(text);
}

int main()
{
    std::srand(static_cast<unsigned int>(std::time(nullptr)));

    sf::RenderWindow window(
        sf::VideoMode({ static_cast<unsigned>(VIEWPORT_W * TILE_SIZE * SCALE),
                       static_cast<unsigned>(VIEWPORT_H * TILE_SIZE * SCALE) }),
        "Princess Marina - RPG Adapter Pattern"
    );
    window.setFramerateLimit(60);

    sf::Font font;
    if (!font.openFromFile("C:/Windows/Fonts/arial.ttf"))
    {
        std::cerr << "Error: Could not load font\n";
        return -1;
    }

    std::vector<std::string> prologueLines = {
        "Year 2525. Princess Marina is a time-traveling warrior",
        "from the future, armed with MODERN weapons.",
        "",
        "But a portal malfunction sends her to medieval times,",
        "where Prince Radmir has been captured by dark forces.",
        "",
        "Marina's modern weapons are losing power...",
        "She must ADAPT ancient artifacts to work with her",
        "advanced combat system!",
        "",
        "This is the story of the ADAPTER PATTERN:",
        "Making OLD weapons work in a NEW system!",
        "",
        "CONTROLS:",
        "WASD - Move",
        "SPACE - Attack",
        "1-2-3 - Switch weapons",
        "",
        "Press SPACE to begin..."
    };

    sf::Text storyText(font);
    storyText.setCharacterSize(16);
    storyText.setFillColor(sf::Color(220, 220, 255));
    storyText.setPosition({ 40, 40 });

    float textTimer = 0.0f;
    float charDelay = 0.02f;
    size_t visibleChars = 0;

    std::string fullText;
    for (const auto& line : prologueLines)
        fullText += line + "\n";

    int fadeAlpha = 0;
    bool fadingIn = true;

    sf::View camera;
    camera.setSize({ static_cast<float>(VIEWPORT_W * TILE_SIZE * SCALE),
                    static_cast<float>(VIEWPORT_H * TILE_SIZE * SCALE) });
    camera.setCenter({ static_cast<float>(VIEWPORT_W * TILE_SIZE * SCALE / 2.0f),
                      static_cast<float>(VIEWPORT_H * TILE_SIZE * SCALE / 2.0f) });

    // ===== LOAD ALL TEXTURES =====
    sf::Texture grassTex, tree1Tex, tree2Tex, sandTex, wallTex, roofTex;
    sf::Texture marinaTex, princeTex, goblinTex;
    sf::Texture pistolTex, swordTex, bowTex;
    sf::Texture chickenTex, cowTex;

    if (!grassTex.loadFromFile("assets/Tiles/tile_0001.png")) {
        std::cerr << "Error loading grassTex\n";
    }
    if (!tree1Tex.loadFromFile("assets/Tiles/tile_0005.png")) {
        std::cerr << "Error loading tree1Tex\n";
    }
    if (!tree2Tex.loadFromFile("assets/Tiles/tile_0003.png")) {
        std::cerr << "Error loading tree2Tex\n";
    }
    if (!sandTex.loadFromFile("assets/Tiles/tile_0025.png")) {
        std::cerr << "Error loading sandTex\n";
    }
    if (!wallTex.loadFromFile("assets/Tiles/tile_0070.png")) {
        std::cerr << "Error loading wallTex\n";
    }
    if (!roofTex.loadFromFile("assets/Tiles/tile_0120.png")) {
        std::cerr << "Error loading roofTex\n";
    }

    if (!marinaTex.loadFromFile("assets/Characters/tile_0084.png")) {
        std::cerr << "Error loading marinaTex\n";
    }
    if (!princeTex.loadFromFile("assets/Characters/tile_0098.png")) {
        std::cerr << "Error loading princeTex\n";
    }
    if (!goblinTex.loadFromFile("assets/Characters/tile_0120.png")) {
        std::cerr << "Error loading goblinTex\n";
    }

    if (!pistolTex.loadFromFile("assets/MicroRoguelike/tile_0078.png")) {
        std::cerr << "Error loading pistolTex\n";
    }
    if (!swordTex.loadFromFile("assets/Characters/tile_0135.png")) {
        std::cerr << "Error loading swordTex\n";
    }
    if (!bowTex.loadFromFile("assets/Characters/tile_0136.png")) {
        std::cerr << "Error loading bowTex\n";
    }

    if (!chickenTex.loadFromFile("assets/Animals/chicken.png")) {
        std::cerr << "Error loading chickenTex\n";
    }
    if (!cowTex.loadFromFile("assets/Animals/cow.png")) {
        std::cerr << "Error loading cowTex\n";
    }

    // ===== CREATE TILE SPRITES =====
    auto grassSpr = std::make_shared<sf::Sprite>(grassTex);
    auto tree1Spr = std::make_shared<sf::Sprite>(tree1Tex);
    auto tree2Spr = std::make_shared<sf::Sprite>(tree2Tex);
    auto sandSpr = std::make_shared<sf::Sprite>(sandTex);
    auto wallSpr = std::make_shared<sf::Sprite>(wallTex);
    auto roofSpr = std::make_shared<sf::Sprite>(roofTex);

    grassSpr->setScale({ SCALE, SCALE });
    tree1Spr->setScale({ SCALE, SCALE });
    tree2Spr->setScale({ SCALE, SCALE });
    sandSpr->setScale({ SCALE, SCALE });
    wallSpr->setScale({ SCALE, SCALE });
    roofSpr->setScale({ SCALE, SCALE });

    // ===== MARINA ENTITY =====
    Entity marina;
    marina.x = 5.0f * TILE_SIZE * SCALE;
    marina.y = 5.0f * TILE_SIZE * SCALE;
    marina.hp = marina.maxHp = 100;
    marina.name = "Marina";
    marina.sprite = std::make_shared<sf::Sprite>(marinaTex);
    marina.sprite->setScale({ SCALE, SCALE });

    // ===== PRINCE ENTITY =====
    Entity prince;
    prince.x = 10.0f * TILE_SIZE * SCALE;
    prince.y = 8.0f * TILE_SIZE * SCALE;
    prince.hp = prince.maxHp = 100;
    prince.name = "Prince Radmir";
    prince.sprite = std::make_shared<sf::Sprite>(princeTex);
    prince.sprite->setScale({ SCALE, SCALE });

    // ===== MONSTERS =====
    std::vector<Entity> monsters;

    for (int i = 0; i < 3; i++)
    {
        Entity monster;
        monster.x = static_cast<float>((13 + i * 7) * TILE_SIZE * SCALE);
        monster.y = static_cast<float>((10 + i * 5) * TILE_SIZE * SCALE);
        monster.hp = monster.maxHp = 30 + std::rand() % 40;
        monster.name = "Goblin Lv" + std::to_string(i + 1);
        monster.targetX = monster.x;
        monster.targetY = monster.y;
        monster.sprite = std::make_shared<sf::Sprite>(goblinTex);
        monster.sprite->setScale({ SCALE, SCALE });

        monsters.push_back(monster);
    }

    // ===== WEAPONS ON MAP =====
    std::vector<WeaponPickup> weapons;

    WeaponPickup pistol;
    pistol.x = 8.0f * TILE_SIZE * SCALE;
    pistol.y = 6.0f * TILE_SIZE * SCALE;
    pistol.isModern = true;
    pistol.name = "Laser Pistol";
    pistol.sprite = std::make_shared<sf::Sprite>(pistolTex);
    pistol.sprite->setScale({ SCALE, SCALE });
    weapons.push_back(pistol);

    WeaponPickup sword;
    sword.x = 18.0f * TILE_SIZE * SCALE;
    sword.y = 15.0f * TILE_SIZE * SCALE;
    sword.isModern = false;
    sword.name = "Ancient Sword";
    sword.sprite = std::make_shared<sf::Sprite>(swordTex);
    sword.sprite->setScale({ SCALE, SCALE });
    weapons.push_back(sword);

    WeaponPickup bow;
    bow.x = 25.0f * TILE_SIZE * SCALE;
    bow.y = 20.0f * TILE_SIZE * SCALE;
    bow.isModern = false;
    bow.name = "Ancient Bow";
    bow.sprite = std::make_shared<sf::Sprite>(bowTex);
    bow.sprite->setScale({ SCALE, SCALE });
    weapons.push_back(bow);

    // ===== ANIMALS =====
    std::vector<Animal> animals;

    for (int i = 0; i < 5; i++)
    {
        Animal chicken;
        chicken.x = static_cast<float>((5 + i * 4) * TILE_SIZE * SCALE);
        chicken.y = static_cast<float>((8 + (std::rand() % 5)) * TILE_SIZE * SCALE);
        chicken.type = "chicken";
        chicken.sprite = std::make_shared<sf::Sprite>(chickenTex);
        chicken.sprite->setScale({ SCALE * 0.2f, SCALE * 0.2f });
        animals.push_back(chicken);
    }

    // ===== GAME VARIABLES =====
    float speed = 120.0f;
    float monsterSpeed = 80.0f;
    int selectedWeapon = 0;

    std::vector<std::string> inventory;
    inventory.push_back("Fists");

    sf::Clock clock;

    // ===== UI ELEMENTS =====
    sf::RectangleShape healthBarBg({ 150, 20 });
    healthBarBg.setFillColor(sf::Color(50, 50, 50));
    healthBarBg.setPosition({ 10, 10 });

    sf::RectangleShape healthBar({ 150, 20 });
    healthBar.setFillColor(sf::Color(220, 50, 50));
    healthBar.setPosition({ 10, 10 });

    sf::Text uiText(font);
    uiText.setCharacterSize(14);
    uiText.setFillColor(sf::Color::White);
    uiText.setPosition({ 10, 35 });

    sf::Text weaponText(font);
    weaponText.setCharacterSize(12);
    weaponText.setFillColor(sf::Color(255, 215, 0));
    weaponText.setPosition({ 10, 55 });

    sf::Text consoleText(font);
    consoleText.setCharacterSize(12);
    consoleText.setFillColor(sf::Color(0, 255, 0));
    consoleText.setOutlineColor(sf::Color::Black);
    consoleText.setOutlineThickness(2);
    consoleText.setPosition({ 10, 580 });
    std::string consoleLog = "";

    // Game over screen
    sf::RectangleShape gameOverOverlay({ static_cast<float>(VIEWPORT_W * TILE_SIZE * SCALE),
                                        static_cast<float>(VIEWPORT_H * TILE_SIZE * SCALE) });
    gameOverOverlay.setFillColor(sf::Color(0, 0, 0, 180));

    sf::Text gameOverText(font);
    gameOverText.setCharacterSize(48);
    gameOverText.setFillColor(sf::Color(255, 0, 0));
    gameOverText.setString("GAME OVER");
    sf::FloatRect bounds = gameOverText.getLocalBounds();
    gameOverText.setOrigin({ bounds.size.x / 2, bounds.size.y / 2 });
    gameOverText.setPosition({ static_cast<float>(VIEWPORT_W * TILE_SIZE * SCALE / 2),
                              static_cast<float>(VIEWPORT_H * TILE_SIZE * SCALE / 2 - 50) });

    sf::Text restartText(font);
    restartText.setCharacterSize(24);
    restartText.setFillColor(sf::Color(255, 255, 255));
    restartText.setString("Press SPACE to restart or ESC to quit");
    bounds = restartText.getLocalBounds();
    restartText.setOrigin({ bounds.size.x / 2, bounds.size.y / 2 });
    restartText.setPosition({ static_cast<float>(VIEWPORT_W * TILE_SIZE * SCALE / 2),
                             static_cast<float>(VIEWPORT_H * TILE_SIZE * SCALE / 2 + 50) });

    // Victory screen
    sf::RectangleShape victoryOverlay({ static_cast<float>(VIEWPORT_W * TILE_SIZE * SCALE),
                                       static_cast<float>(VIEWPORT_H * TILE_SIZE * SCALE) });
    victoryOverlay.setFillColor(sf::Color(0, 0, 0, 180));

    sf::Text victoryText(font);
    victoryText.setCharacterSize(48);
    victoryText.setFillColor(sf::Color(0, 255, 0));
    victoryText.setString("VICTORY!");
    bounds = victoryText.getLocalBounds();
    victoryText.setOrigin({ bounds.size.x / 2, bounds.size.y / 2 });
    victoryText.setPosition({ static_cast<float>(VIEWPORT_W * TILE_SIZE * SCALE / 2),
                             static_cast<float>(VIEWPORT_H * TILE_SIZE * SCALE / 2 - 50) });

    sf::Text victorySubText(font);
    victorySubText.setCharacterSize(24);
    victorySubText.setFillColor(sf::Color(255, 255, 0));
    victorySubText.setString("Prince Radmir has been rescued!");
    bounds = victorySubText.getLocalBounds();
    victorySubText.setOrigin({ bounds.size.x / 2, bounds.size.y / 2 });
    victorySubText.setPosition({ static_cast<float>(VIEWPORT_W * TILE_SIZE * SCALE / 2),
                                static_cast<float>(VIEWPORT_H * TILE_SIZE * SCALE / 2 + 20) });

    bool isGameOver = false;
    bool metPrince = false;  // Флаг встречи с принцем

    float attackCooldown = 0.0f;
    float monsterAttackCooldown = 0.0f;

    // ===== MAIN LOOP =====
    while (window.isOpen())
    {
        float dt = clock.restart().asSeconds();

        while (auto event = window.pollEvent())
        {
            if (event->is<sf::Event::Closed>())
                window.close();

            if (event->is<sf::Event::KeyPressed>())
            {
                auto keyEvent = event->getIf<sf::Event::KeyPressed>();

                // Escape to quit
                if (keyEvent && keyEvent->code == sf::Keyboard::Key::Escape)
                {
                    window.close();
                }

                // Game over restart
                if (isGameOver && keyEvent && keyEvent->code == sf::Keyboard::Key::Space)
                {
                    window.close();
                    return 0;
                }

                if (keyEvent && keyEvent->code == sf::Keyboard::Key::Space && !isGameOver)
                {
                    if (currentState == PROLOGUE)
                    {
                        if (visibleChars < fullText.size())
                            visibleChars = fullText.size();
                        else
                            currentState = PLAYING;
                    }
                    else if (currentState == PLAYING && attackCooldown <= 0)
                    {
                        attackCooldown = 0.5f;

                        for (auto& monster : monsters)
                        {
                            if (!monster.alive) continue;

                            float dist = std::sqrt((marina.x - monster.x) * (marina.x - monster.x) +
                                (marina.y - monster.y) * (marina.y - monster.y));

                            if (dist < 80.0f)
                            {
                                int damage = 15 + std::rand() % 10;
                                monster.hp -= damage;

                                if (selectedWeapon > 0)
                                {
                                    consoleLog = "[WeaponAdapter] Attack() -> Strike() - ADAPTING!\n";
                                    consoleLog += "Ancient weapon adapted! Damage: " + std::to_string(damage);
                                }
                                else
                                {
                                    consoleLog = "[ModernWeapon] Direct Attack() - NO ADAPTER!\n";
                                    consoleLog += "Modern weapon! Damage: " + std::to_string(damage);
                                }

                                std::cout << consoleLog << "\n";

                                if (monster.hp <= 0)
                                {
                                    monster.alive = false;
                                    consoleLog += "\nMonster defeated! 'The prince is in the northern castle...'";
                                }
                                break;
                            }
                        }
                    }
                }

                if (currentState == PLAYING && keyEvent && !isGameOver)
                {
                    if (keyEvent->code == sf::Keyboard::Key::Num1 ||
                        keyEvent->code == sf::Keyboard::Key::Numpad1)
                    {
                        selectedWeapon = 0;
                    }
                    else if ((keyEvent->code == sf::Keyboard::Key::Num2 ||
                        keyEvent->code == sf::Keyboard::Key::Numpad2) && inventory.size() > 1)
                    {
                        selectedWeapon = 1;
                    }
                    else if ((keyEvent->code == sf::Keyboard::Key::Num3 ||
                        keyEvent->code == sf::Keyboard::Key::Numpad3) && inventory.size() > 2)
                    {
                        selectedWeapon = 2;
                    }
                    else if (keyEvent->code == sf::Keyboard::Key::E)
                    {
                        // Проверяем расстояние до принца
                        float distToPrince = std::sqrt((marina.x - prince.x) * (marina.x - prince.x) +
                            (marina.y - prince.y) * (marina.y - prince.y));

                        if (distToPrince < 80.0f)
                        {
                            metPrince = true;
                            consoleLog = "[QUEST COMPLETE] Marina has rescued Prince Radmir!\n";
                            consoleLog += "The Adapter Pattern saved the kingdom!";
                            std::cout << consoleLog << "\n";
                        }
                        else
                        {
                            consoleLog = "You must get closer to the Prince...";
                        }
                    }
                }
            }
        }

        if (currentState == PROLOGUE)
        {
            if (fadingIn)
            {
                fadeAlpha += static_cast<int>(dt * 100);
                if (fadeAlpha >= 255)
                {
                    fadeAlpha = 255;
                    fadingIn = false;
                }
            }

            textTimer += dt;
            if (textTimer >= charDelay && visibleChars < fullText.size())
            {
                textTimer = 0.0f;
                visibleChars++;
            }

            storyText.setString(fullText.substr(0, visibleChars));
            storyText.setFillColor(sf::Color(220, 220, 255, static_cast<unsigned char>(fadeAlpha)));

            window.clear(sf::Color::Black);
            window.setView(window.getDefaultView());
            window.draw(storyText);
            window.display();
            continue;
        }

        // PLAYING state
        if (!isGameOver)
        {
            attackCooldown -= dt;
            if (attackCooldown < 0) attackCooldown = 0;

            // Player movement
            marina.vx = 0;
            marina.vy = 0;

            if (sf::Keyboard::isKeyPressed(sf::Keyboard::Key::W)) marina.vy = -speed;
            if (sf::Keyboard::isKeyPressed(sf::Keyboard::Key::S)) marina.vy = speed;
            if (sf::Keyboard::isKeyPressed(sf::Keyboard::Key::A)) { marina.vx = -speed; marina.facingRight = false; }
            if (sf::Keyboard::isKeyPressed(sf::Keyboard::Key::D)) { marina.vx = speed; marina.facingRight = true; }

            marina.x += marina.vx * dt;
            marina.y += marina.vy * dt;

            if (marina.vx != 0 || marina.vy != 0)
            {
                marina.animTimer += dt * 10;
            }

            // Weapon pickup
            for (auto& w : weapons)
            {
                if (!w.pickedUp)
                {
                    float dist = std::sqrt((marina.x - w.x) * (marina.x - w.x) +
                        (marina.y - w.y) * (marina.y - w.y));
                    if (dist < 40.0f)
                    {
                        w.pickedUp = true;
                        inventory.push_back(w.name);

                        if (w.isModern)
                        {
                            consoleLog = "[ModernWeapon] Picked up: " + w.name + " - Direct implementation of IWeapon!";
                        }
                        else
                        {
                            consoleLog = "[WeaponAdapter] Picked up: " + w.name + " - Adapting OldWeapon to IWeapon!";
                        }
                        std::cout << consoleLog << "\n";
                    }
                }
            }

            // Weapon rotation animation
            for (auto& w : weapons)
            {
                if (!w.pickedUp)
                {
                    w.rotationAngle += dt * 100;
                }
            }

            // Monster AI
            monsterAttackCooldown -= dt;
            if (monsterAttackCooldown < 0) monsterAttackCooldown = 0;

            for (auto& monster : monsters)
            {
                if (!monster.alive) continue;

                float distToMarina = std::sqrt((marina.x - monster.x) * (marina.x - monster.x) +
                    (marina.y - monster.y) * (marina.y - monster.y));

                if (distToMarina < 200.0f)
                {
                    float dx = marina.x - monster.x;
                    float dy = marina.y - monster.y;
                    float len = std::sqrt(dx * dx + dy * dy);

                    if (len > 60.0f)
                    {
                        monster.vx = (dx / len) * monsterSpeed;
                        monster.vy = (dy / len) * monsterSpeed;
                        monster.facingRight = dx > 0;
                    }
                    else
                    {
                        monster.vx = 0;
                        monster.vy = 0;

                        if (monsterAttackCooldown <= 0)
                        {
                            monsterAttackCooldown = 1.5f;
                            marina.hp -= 10;

                            consoleLog = "[Monster] " + monster.name + " attacks Marina! -10 HP";
                            std::cout << consoleLog << "\n";

                            if (marina.hp <= 0)
                            {
                                marina.hp = 0;
                                isGameOver = true;
                                consoleLog = "GAME OVER! Marina was defeated...";
                            }
                        }
                    }
                }
                else
                {
                    if (std::abs(monster.x - monster.targetX) < 5 && std::abs(monster.y - monster.targetY) < 5)
                    {
                        monster.targetX = monster.x + static_cast<float>(std::rand() % 100 - 50);
                        monster.targetY = monster.y + static_cast<float>(std::rand() % 100 - 50);
                    }

                    float dx = monster.targetX - monster.x;
                    float dy = monster.targetY - monster.y;
                    float len = std::sqrt(dx * dx + dy * dy);

                    if (len > 5)
                    {
                        monster.vx = (dx / len) * (monsterSpeed * 0.5f);
                        monster.vy = (dy / len) * (monsterSpeed * 0.5f);
                    }
                }

                monster.x += monster.vx * dt;
                monster.y += monster.vy * dt;
                monster.animTimer += dt * 5;
            }

            // Animals wandering
            for (auto& animal : animals)
            {
                animal.wanderTimer -= dt;

                if (animal.wanderTimer <= 0)
                {
                    animal.wanderTimer = 2.0f + (std::rand() % 30) / 10.0f;
                    animal.vx = static_cast<float>(std::rand() % 60 - 30);
                    animal.vy = static_cast<float>(std::rand() % 60 - 30);
                }

                animal.x += animal.vx * dt;
                animal.y += animal.vy * dt;
                animal.animTimer += dt * 8;
            }

            prince.animTimer += dt * 2;
        }

        // Camera follow Marina
        camera.setCenter({ marina.x, marina.y });
        window.setView(camera);
        window.clear(sf::Color(34, 139, 34));

        // Render map
        float halfViewW = (VIEWPORT_W * TILE_SIZE * SCALE) / 2.0f;
        float halfViewH = (VIEWPORT_H * TILE_SIZE * SCALE) / 2.0f;

        int startX = static_cast<int>((marina.x - halfViewW) / (TILE_SIZE * SCALE)) - 1;
        int endX = static_cast<int>((marina.x + halfViewW) / (TILE_SIZE * SCALE)) + 2;
        int startY = static_cast<int>((marina.y - halfViewH) / (TILE_SIZE * SCALE)) - 1;
        int endY = static_cast<int>((marina.y + halfViewH) / (TILE_SIZE * SCALE)) + 2;

        for (int y = startY; y < endY; y++)
        {
            for (int x = startX; x < endX; x++)
            {
                float px = x * TILE_SIZE * SCALE;
                float py = y * TILE_SIZE * SCALE;

                grassSpr->setPosition({ px, py });
                window.draw(*grassSpr);

                int tile = getTileAt(x, y);
                switch (tile)
                {
                case 1: tree1Spr->setPosition({ px, py }); window.draw(*tree1Spr); break;
                case 2: tree2Spr->setPosition({ px, py }); window.draw(*tree2Spr); break;
                case 3: sandSpr->setPosition({ px, py }); window.draw(*sandSpr); break;
                case 4: wallSpr->setPosition({ px, py }); window.draw(*wallSpr); break;
                case 5: roofSpr->setPosition({ px, py }); window.draw(*roofSpr); break;
                }
            }
        }

        // Draw weapons on map
        for (auto& w : weapons)
        {
            if (!w.pickedUp && w.sprite)
            {
                float yOffset = std::sin(w.rotationAngle / 50.0f) * 5;
                w.sprite->setPosition({ w.x, w.y + yOffset });
                w.sprite->setRotation(sf::degrees(w.rotationAngle));
                window.draw(*w.sprite);

                sf::CircleShape glow(12);
                glow.setPosition({ w.x - 12, w.y - 12 + yOffset });
                glow.setFillColor(sf::Color::Transparent);
                glow.setOutlineThickness(2);
                glow.setOutlineColor(w.isModern ? sf::Color(0, 200, 255, 150) : sf::Color(255, 215, 0, 150));
                window.draw(glow);
            }
        }

        // Draw animals
        for (auto& animal : animals)
        {
            if (animal.sprite)
            {
                float bobbing = std::sin(animal.animTimer) * 2;
                animal.sprite->setPosition({ animal.x, animal.y + bobbing });
                window.draw(*animal.sprite);
            }
        }

        // Draw prince
        if (prince.sprite)
        {
            float princeBobbing = std::sin(prince.animTimer) * 3;
            float princeScale = SCALE + std::sin(prince.animTimer) * 0.1f;
            prince.sprite->setScale({ princeScale, princeScale });
            prince.sprite->setPosition({ prince.x, prince.y + princeBobbing });
            window.draw(*prince.sprite);
            drawHealthBar(window, prince.x, prince.y, prince.hp, prince.maxHp);
            drawNameTag(window, font, prince.x, prince.y, prince.name, sf::Color(255, 215, 0));
        }

        // Draw monsters
        for (auto& monster : monsters)
        {
            if (!monster.alive || !monster.sprite) continue;

            float bobbing = std::sin(monster.animTimer) * 2;
            monster.sprite->setPosition({ monster.x, monster.y + bobbing });

            if (monster.facingRight)
                monster.sprite->setScale({ SCALE, SCALE });
            else
                monster.sprite->setScale({ -SCALE, SCALE });

            window.draw(*monster.sprite);
            drawHealthBar(window, monster.x, monster.y, monster.hp, monster.maxHp);
            drawNameTag(window, font, monster.x, monster.y, monster.name, sf::Color(255, 100, 100));
        }

        // Draw Marina
        if (marina.sprite)
        {
            float marinaBobbing = (marina.vx != 0 || marina.vy != 0) ? std::sin(marina.animTimer) * 3 : 0;
            marina.sprite->setPosition({ marina.x, marina.y + marinaBobbing });

            if (marina.facingRight)
                marina.sprite->setScale({ SCALE, SCALE });
            else
                marina.sprite->setScale({ -SCALE, SCALE });

            window.draw(*marina.sprite);
            drawHealthBar(window, marina.x, marina.y, marina.hp, marina.maxHp);
            drawNameTag(window, font, marina.x, marina.y, marina.name, sf::Color(255, 100, 255));
        }

        // UI
        window.setView(window.getDefaultView());

        window.draw(healthBarBg);

        float hpBarWidth = 150.0f * (static_cast<float>(marina.hp) / marina.maxHp);
        healthBar.setSize({ hpBarWidth, 20 });
        window.draw(healthBar);

        uiText.setString("HP: " + std::to_string(marina.hp) + "/" + std::to_string(marina.maxHp));
        window.draw(uiText);

        std::string weaponInfo = "Weapon [" + std::to_string(selectedWeapon + 1) + "]: ";
        if (selectedWeapon < static_cast<int>(inventory.size()))
        {
            weaponInfo += inventory[selectedWeapon];

            if (selectedWeapon == 0)
                weaponInfo += " (Modern - Direct)";
            else
                weaponInfo += " (Ancient - ADAPTER!)";
        }
        weaponText.setString(weaponInfo);
        weaponText.setFillColor(selectedWeapon == 0 ? sf::Color(0, 200, 255) : sf::Color(255, 215, 0));
        window.draw(weaponText);

        consoleText.setString(consoleLog);
        window.draw(consoleText);

        sf::Text tutorialText(font);
        tutorialText.setCharacterSize(12);
        tutorialText.setFillColor(sf::Color(200, 200, 200));
        tutorialText.setOutlineColor(sf::Color::Black);
        tutorialText.setOutlineThickness(1);
        tutorialText.setPosition({ 10, 605 });
        tutorialText.setString("SPACE-Attack | 1-2-3-Weapons | E-Interact | Collect weapons & defeat monsters!");
        window.draw(tutorialText);

        // Draw game over screen if needed
        if (isGameOver)
        {
            window.setView(window.getDefaultView());
            window.draw(gameOverOverlay);
            window.draw(gameOverText);
            window.draw(restartText);
        }

        // Draw victory screen if needed
        if (metPrince)
        {
            window.setView(window.getDefaultView());
            window.draw(victoryOverlay);
            window.draw(victoryText);
            window.draw(victorySubText);
            window.draw(restartText);
        }

        window.display();
    }

    return 0;
}