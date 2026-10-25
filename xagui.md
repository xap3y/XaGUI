# XaGUI: návod pro vytváření GUI v pluginech

Tento dokument je návod pro vývojáře a AI. Vychází ze zdrojů XaGUI v tomto repozitáři a z použití v EggHunt-v2. Při implementaci respektuj také strukturu, lokalizaci a konfiguraci cílového pluginu.

## Základní pravidla

- Každé běžné menu vytvoř jako samostatnou Java třídu `extends VirtualMenu<T>`.
- `T` nahraď konkrétním typem kontextu, například `Player`, `String` nebo vlastním DTO. `VirtualMenu<?>` je pouze obecné označení, nikoli zápis pro deklaraci potomka. Pokud se to nechce vyuzit, nastav to treba na Byte a dej null do parametru k metode build()
- Přepiš `public @NotNull GuiMenuInterface build(@NotNull T ctx)`.
- Uvnitř vezmi `GuiMenuInterface gui = getGui();`, uprav ho a vrať `return gui;`.
- Otevírej přes `new MojeMenu(xagui).build(ctx).open(player)` nebo `new MojeMenu(xagui).open(player, ctx)`.
- Používej `GuiButton`, jeho `.withListener(...)` a `.withClickSound(...)`.
- Border, zavření a stránkování řeš vestavěnými metodami. Nevytvářej vlastní `PagedGui`, vlastní systém inventářů ani další click listener jen kvůli obsluze tlačítek.
- Pro nezávislé otevření vytvářej novou instanci menu. Menu drží měnitelný obsah, listenery a aktuální stránku; nesdílej jednu instanci mezi hráči s různými daty.
- Kód piš přehledně na více řádků, včetně lambda bloků.

## Inicializace XaGui

Plugin obvykle uchovává jednu instanci `XaGui`, kterou předává menu nebo poskytuje getterem jako EggHunt-v2 přes `McGeocaching.getXagui()`.

Při vložení knihovny do vlastního pluginu ji inicializuj jednou v `onEnable()`:

```java
XaGui xagui = new XaGui(this);
```

Konstruktor registruje obsluhu událostí. Nevytvářej `new XaGui(...)` při každém otevření menu.

Pokud projekt používá samostatný XaGUI plugin, převezmi jeho instanci podle existující integrace:

```java
XaGui xagui = XaGuiPlugin.getXaGui();
```

V tomto režimu zajisti závislost a pořadí načtení pluginů. Nekombinuj obě varianty inicializace bez důvodu. Globální nastavení tlačítek a zvuků jsou v aktuální knihovně uložena staticky, takže nejsou izolována podle instance `XaGui`.

Základní importy:

```java
import eu.xap3y.xagui.VirtualMenu;
import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import eu.xap3y.xagui.models.GuiButton;
import org.jetbrains.annotations.NotNull;
```

`GuiMenuInterface` patří do balíčku `eu.xap3y.xagui.interfaces`.

## Jednostránkové menu

Kompletní příklad menu s kontextem typu `Player`:

```java
import eu.xap3y.xagui.VirtualMenu;
import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import eu.xap3y.xagui.models.GuiButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ProfileMenu extends VirtualMenu<Player> {

    public ProfileMenu(XaGui xagui) {
        super("&8Profil", 3, xagui);
    }

    @Override
    public @NotNull GuiMenuInterface build(@NotNull Player ctx) {
        GuiMenuInterface gui = getGui();

        gui.fillBorder();
        gui.addCloseButton();

        GuiButton profile = new GuiButton(Material.PLAYER_HEAD)
                .setName("&a" + ctx.getName())
                .setLore(
                        "&7Informace o hráči",
                        "&eKlikni pro zobrazení jména."
                )
                .withClickSound(Sound.UI_BUTTON_CLICK, 0.8f)
                .withListener(event -> {
                    Player player = event.getPlayer();
                    player.sendMessage("Profil: " + ctx.getName());
                });

        gui.setSlot(13, profile);

        return gui;
    }
}
```

`super(title, rows, xagui)` vytvoří podkladové GUI s jednou stránkou. Počet řádků bývá 1–6, každý řádek má 9 slotů. Sloty se číslují od nuly. Tři řádky tedy obsahují sloty `0` až `26`.

Konstruktor může použít i `super(title, rows, pages, xagui)`, pokud je počet stránek znám předem. Uvnitř `build` nevytvářej další `GuiMenu`: upravuj instanci získanou přes `getGui()`.

Názvy a texty v příkladech jsou ilustrační. Pokud plugin používá locale/config, čti je z něj.

## Jak menu otevřít

Doporučený explicitní zápis:

```java
new ProfileMenu(xagui).build(player).open(player);
```

Stejné sestavení a otevření umí overload s kontextem:

```java
new ProfileMenu(xagui).open(player, player);
```

První argument je příjemce menu, druhý argument je kontext pro `build(ctx)`. Nemusí to být stejný objekt ani stejný typ.

Otevření konkrétní stránky po sestavení:

```java
GuiMenuInterface gui = new ProfileMenu(xagui).build(player);
gui.open(0, player);
```

**Pozor:** `VirtualMenu.open(player)`, `VirtualMenu.open(page, player)` ani bezparametrové `VirtualMenu.build()` nevolají vlastní `build(ctx)`. Pouze pracují s již existujícím GUI. Samotné `new ProfileMenu(xagui).open(player)` tak neprovede sestavení obsahu z příkladu výše.

Pokud menu nepotřebuje data, použij například `VirtualMenu<Boolean>` a `build(Boolean.TRUE)`. Nepředávej `null` do parametru označeného `@NotNull`, i když některé starší příklady v EggHunt-v2 používají `build(null)`.

GUI sestavuj a měň na správném serverovém vlákně. U běžného Bukkit/Paper pluginu načti případná data z databáze asynchronně a následné sestavení a otevření přesuň na hlavní vlákno. Naplánování samotného otevření uvnitř XaGUI neznamená, že je bezpečné asynchronně vytvářet inventáře a upravovat jejich obsah.

## Border a zavírací tlačítko

```java
gui.fillBorder();
gui.addCloseButton();
```

- `fillBorder()` vyplní okraje všech aktuálně vytvořených stránek globálním border itemem.
- `addCloseButton()` přidá nakonfigurované zavírací tlačítko pouze na stránku s indexem `0`.
- `addCloseButtonAllPages()` ho přidá na všechny stránky.

Zavírací tlačítko je uprostřed posledního řádku: slot `(rows - 1) * 9 + 4`. U GUI se šesti řádky jde o slot `49`.

Nejdřív vyplň border, až potom přidej tlačítka. Obrácené pořadí by tlačítka na okraji přepsalo fillerem.

Vlastní vzhled borderu lze nastavit globálně:

```java
xagui.setBorderItem(
        new GuiButton(Material.GRAY_STAINED_GLASS_PANE)
                .setName(" ")
                .getItem()
);
```

Existují také `gui.fillBorder(itemStack)`, `gui.fillBorder(material)` a `gui.fillBorder(pageIndex, itemStack)`. Na rozdíl od bezparametrového `fillBorder()` první dvě varianty vyplňují pouze aktuální stránku.

### Globální close tlačítko a zvuk

Ve stylu EggHunt-v2:

```java
xagui.setCloseButton(StaticGuiItems.CLOSE.getButton());
xagui.setCloseButtonSound(Sound.BLOCK_COPPER_DOOR_CLOSE);
```

`StaticGuiItems` je vlastní enum cílového pluginu, není součástí XaGUI. Nenavrhuj import `StaticGuiItems` z knihovny XaGUI. Pokud projekt takový enum má, používej ho; jinak vytvoř tlačítko pomocí `GuiButton`.

**Důležité chování aktuální implementace:** `setCloseButton(...)` nahrazuje celé tlačítko včetně listeneru. `addCloseButton()` ho pouze umístí do GUI. Vlastní tlačítko proto musí mít listener, který inventář zavře. `setCloseButtonSound(sound)` nastavuje zvuk používaný výchozím close listenerem XaGUI; automaticky ho nepřidává do listeneru vlastního tlačítka. EggHunt-v2 zavření i zvuk řeší ve vlastním `StaticGuiItems.CLOSE`.

Příklad vlastního tlačítka se zvukem:

```java
GuiButton closeButton = new GuiButton(Material.BARRIER)
        .setName("&cZavřít")
        .withClickSound(Sound.BLOCK_COPPER_DOOR_CLOSE, 0.5f)
        .withListener(event -> {
            event.getWhoClicked().closeInventory();
        });

xagui.setCloseButton(closeButton);
```

`setCloseButtonSound` přijímá argument `Sound`; bezparametrová varianta v tomto API není.

## GuiButton: vzhled, kliknutí a zvuk

Tlačítko lze vytvořit z `Material` nebo existujícího `ItemStack`. Akci připojuj přímo přes `.withListener(...)`:

```java
GuiButton button = new GuiButton(Material.CHEST)
        .setName("&6Odměny")
        .setLore("&7Tvoje dostupné odměny.")
        .withClickSound(Sound.UI_BUTTON_CLICK, 0.8f)
        .withListener(event -> {
            Player player = event.getPlayer();

            if (event.isLeftClick()) {
                player.sendMessage("Vybrána odměna.");
            }
        });

gui.setSlot(13, button);
```

Listener dostává `GuiClickEvent`, který rozšiřuje `InventoryClickEvent`. K dispozici jsou například `getPlayer()`, `getWhoClicked()`, `getClick()`, `isLeftClick()`, `isRightClick()`, `isShiftClick()` a `getSlot()`.

`.withListener(...)` nastavuje jeden listener; dalším voláním předchozí nahradíš. `.withClickSound(sound, volume)` přijímá zvuk a hlasitost, nikoli pitch. Pitch je v aktuální obsluze `1f`.

Další užitečné metody:

| Účel | Metody |
| --- | --- |
| Název a lore | `setName`, `setLore`, `setLoreList`, `addLoreLine`, `addLore`, `clearLore` |
| Počet kusů | `setAmount` |
| Item flags | `addItemFlag`, `removeItemFlag` |
| Enchanty | `addEnchantment`, `removeEnchantment`, `removeAllEnchantments` |
| Podkladový item | `getItem`, `setItem` |
| Kopie tlačítka | `clone` |
| Přechod do menu | `withRedirect` |

`setName` a základní lore metody podporují barvy s `&`. `getItem()` vrací podkladový item, nikoli jeho kopii. Při úpravách sdílených šablon pracuj s kopií. `GuiButton.clone()` klonuje item a přebírá listener/redirect/zvuk, ale aktuálně nekopíruje vlastní hlasitost zvuku; případně ji znovu nastav přes `withClickSound(...)`.

Globální výchozí zvuk nových tlačítek:

```java
xagui.setButtonClickSound(Sound.UI_BUTTON_CLICK, 0.8f);
```

Nastav ho před vytvářením tlačítek. Další globální `xagui.setClickSound(sound)` slouží jako fallback při kliknutí na tlačítko bez vlastního zvuku. Proto `.withClickSound(null, 1.0f)` odstraní zvuk tlačítka, ale nezaručuje ticho, pokud je tento fallback nastaven.

### Přechod do jiného menu

Přímo v listeneru:

```java
GuiButton profileButton = new GuiButton(Material.BOOK)
        .setName("&aProfil")
        .withListener(event -> {
            Player player = event.getPlayer();
            new ProfileMenu(xagui).build(player).open(player);
        });
```

Pro kontext dostupný již při sestavování lze použít supplier:

```java
GuiButton profileButton = new GuiButton(Material.BOOK)
        .setName("&aProfil")
        .withRedirect(() -> new ProfileMenu(xagui).build(ctx));
```

`withRedirect` očekává `Supplier<GuiMenuInterface>`, ne instanci `VirtualMenu`. Supplier se vyhodnotí při kliknutí a XaGUI vrácené GUI otevře. Redirect proběhne po listeneru, pokud jsou nastaveny oba. Nedělej stejné otevření zároveň v listeneru i v redirectu.

## Více stránek a vestavěný paginator

XaGUI má vlastní stránkování. `gui.addPaginator()` vytvoří ovládání přepínání; nerozdělí však data do stránek. Rozmístění položek řeší `build(ctx)`.

Postup:

1. Připrav a vyfiltruj data.
2. Spočítej počet stránek, minimálně `1`.
3. Zavolej `gui.setTotalPages(totalPages)` ještě před vkládáním obsahu.
4. Použij `gui.fillBorder()` a `gui.addCloseButtonAllPages()`.
5. Pokud je stránek více než jedna, zavolej `gui.addPaginator()`.
6. Vlož položky přes `gui.setSlot(pageIndex, slot, button)`.

**`setTotalPages(...)` smaže dosavadní obsah a znovu vytvoří inventáře i mapování tlačítek.** Nepoužívej ho po naplnění GUI jako pouhé nastavení počítadla.

Příklad kompletního stránkovaného menu ve stylu `PlacedEggsView` a `HeadBrowserGui`:

```java
import eu.xap3y.xagui.VirtualMenu;
import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import eu.xap3y.xagui.models.GuiButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ItemsMenu extends VirtualMenu<List<String>> {

    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public ItemsMenu(XaGui xagui) {
        super("&8Položky", 6, xagui);
    }

    @Override
    public @NotNull GuiMenuInterface build(@NotNull List<String> ctx) {
        GuiMenuInterface gui = getGui();

        int itemsPerPage = CONTENT_SLOTS.length;
        int totalPages = Math.max(
                1,
                (int) Math.ceil(ctx.size() / (double) itemsPerPage)
        );

        gui.setTotalPages(totalPages);
        gui.fillBorder();
        gui.addCloseButtonAllPages();

        if (totalPages > 1) {
            gui.addPaginator();
            gui.setPageSwitchSound(Sound.ITEM_BOOK_PAGE_TURN);
        }

        for (int index = 0; index < ctx.size(); index++) {
            String name = ctx.get(index);
            int pageIndex = index / itemsPerPage;
            int slot = CONTENT_SLOTS[index % itemsPerPage];

            GuiButton button = new GuiButton(Material.PAPER)
                    .setName("&e" + name)
                    .withClickSound(Sound.UI_BUTTON_CLICK, 0.8f)
                    .withListener(event -> {
                        event.getPlayer().sendMessage(name);
                    });

            gui.setSlot(pageIndex, slot, button);
        }

        return gui;
    }
}
```

Otevření:

```java
new ItemsMenu(xagui).build(List.of("První", "Druhá")).open(player);
```

Indexy stránek jsou od nuly. `getCurrentPageIndex()` vrací index, `getCurrentPage()` číslo pro zobrazení od jedničky a `getMaxPages()` nastavený počet stránek.

Paginator používá v posledním řádku slot `(rows - 1) * 9 + 3` pro předchozí a `(rows - 1) * 9 + 5` pro další stránku. U šesti řádků jsou to sloty `48` a `50`, close je na `49`. Tyto sloty nepoužívej pro obsah. Šipky se doplní při otevření/přepnutí podle dostupnosti sousední stránky.

### Nastavení vzhledu paginatoru

Globálně, před sestavováním menu:

```java
xagui.setPreviousPageButton(
        new GuiButton(Material.ARROW)
                .setName("&ePředchozí stránka")
                .getItem()
);

xagui.setNextPageButton(
        new GuiButton(Material.ARROW)
                .setName("&eDalší stránka")
                .getItem()
);
```

Tyto settery přijímají `ItemStack`, proto se používá `.getItem()`. Listener na přepnutí stránky vytváří XaGUI.

Pro konkrétní menu existují také `gui.setPreviousPageButton(item)` a `gui.setNextPageButton(item)`. Volej je **až po `gui.addPaginator()`**, protože `addPaginator()` převezme globální vzhled a lokální nastavení by přepsal.

## Další funkce GuiMenuInterface

| Metoda | Použití |
| --- | --- |
| `setSlot(slot, button)` | Nastaví tlačítko na aktuální stránce. Přijímá i `ItemStack` nebo `Material`. |
| `setSlot(page, slot, button)` | Nastaví tlačítko na konkrétní stránce. |
| `setAllPageSlot(slot, button)` | Umístí tlačítko do stejného slotu na všech stránkách. |
| `updateSlot(slot, item)` | Změní item již existujícího tlačítka a zachová jeho listener. Existuje i varianta s indexem stránky. |
| `getSlot`, `clearSlot` | Získá nebo odstraní tlačítko; existují i varianty s indexem stránky. |
| `clearAllSlots()` | Vyčistí obsah aktuální stránky; znovu přidej i její border a ovládání. |
| `clearAllSlots(page)` | Vyčistí jednu stránku. |
| `fillSlots(...)` | Vyplní vybrané sloty itemem nebo tlačítkem podle overloadu. |
| `stickSlot(slot)` | Při otevření stránky zkopíruje tento slot z první stránky. |
| `unStickSlot(slot)` | Zruší toto kopírování. |
| `setOpenSound(sound, volume)` | Zvuk při otevření; existuje i overload pouze se zvukem. |
| `setPageSwitchSound(sound)` | Zvuk změny stránky. |
| `setOnOpen`, `setOnClose` | Callbacky s Bukkit open/close eventem. |
| `setOnClick` | Obsluha kliknutí v horním GUI. Běžná tlačítka řeš přes `withListener`. |
| `setOnClickOwn` | Obsluha kliknutí ve vlastním inventáři hráče. |
| `setOnPageSwitch` | Callback s `getPlayer()`, `getTargetPage()` a `getOldPage()`. |
| `allowClickTypes`, `blacklistClickTypes` | Filtry typů kliknutí v GUI. |
| `setSelfInventoryAccess`, `allowSelfInventoryClickTypes` | Nastavení manipulace ve vlastním inventáři hráče. |
| `unlockButton`, `lockButton` | Povolení/zakázání manipulace s itemem ve slotu. Nejde o zapnutí/vypnutí listeneru tlačítka. |
| `setCallback`, `callback` | Nastavení a explicitní zavolání vlastního `Runnable`; nezaměňuj se `setOnClose`. |
| `close(player)` | Zavře GUI konkrétnímu hráči. |
| `close()` | Zavře menu divákům aktuální stránky. |

Sloty GUI jsou standardně zamčené proti braní itemů, ale jejich tlačítkové listenery stále fungují. Pro běžné klikací tlačítko není potřeba `unlockButton(...)`.

Povolení manipulace ve vlastním inventáři vyžaduje jak `setSelfInventoryAccess(true)`, tak povolené typy přes `allowSelfInventoryClickTypes(...)`; prázdný seznam je v aktuální implementaci nepovolí.

Při úpravě tlačítka za běhu lze přímo v jeho listeneru použít `event.getClickedButton()` a po úpravě `event.updateClickedButton()`. Pro nahrazení slouží `event.updateClickedButton(newButton)`. Aktualizace samotného itemu přes `getItem()` nezaručuje překreslení bez opětovného nastavení slotu.

## Kontrola před dokončením implementace

- Menu dědí z `VirtualMenu<KonkrétníTyp>` a implementuje `build(ctx)`.
- `build` upravuje `getGui()` a vrací `gui`.
- Před otevřením se skutečně zavolá `build(ctx)`.
- Každé nezávislé otevření používá vlastní instanci menu.
- Border se vyplňuje před vložením ovládacích tlačítek.
- Vícestránkové menu používá `addCloseButtonAllPages()` a vestavěný `addPaginator()`.
- Počet stránek se nastavuje před naplněním a počítá se z již filtrovaných dat.
- Stránky se indexují od nuly; paginator a close sloty zůstávají vyhrazené.
- Vlastní close tlačítko má funkční listener a požadovaný zvuk.
- Obsluha tlačítek je přes `GuiButton.withListener`, zvuky přes `withClickSound` nebo konfiguraci XaGui.
- Texty a vzhled dodržují konvence cílového pluginu.

## Kde ověřit API a příklady

Cesty jsou relativní ke kořenům uvedených projektů:

- XaGUI: `core/src/main/java/eu/xap3y/xagui/VirtualMenu.java` — konstrukce, build a otevírání.
- XaGUI: `core/src/main/java/eu/xap3y/xagui/interfaces/GuiMenuInterface.java` — dostupné metody GUI.
- XaGUI: `core/src/main/java/eu/xap3y/xagui/GuiMenu.java` — implementace stránek, borderu a ovládání.
- XaGUI: `core/src/main/java/eu/xap3y/xagui/models/GuiButton.java` — tlačítka, listenery, redirect a zvuk.
- XaGUI: `core/src/main/java/eu/xap3y/xagui/XaGui.java` — inicializace a globální nastavení.
- EggHunt-v2: `src/main/java/eu/xap3y/mcgeocaching/McGeocaching.java` — inicializace a vzhled ovládání.
- EggHunt-v2: `src/main/java/eu/xap3y/mcgeocaching/api/gui/MainGui.java` — jednoduché menu.
- EggHunt-v2: `src/main/java/eu/xap3y/mcgeocaching/api/gui/PlacedEggsView.java` a `HeadBrowserGui.java` — více stránek.
- EggHunt-v2: `src/main/java/eu/xap3y/mcgeocaching/api/enums/StaticGuiItems.java` — vlastní šablony tlačítek.

Příklady v existujícím pluginu ber jako ukázku stylu. Přesné signatury a chování ověřuj v aktuálních zdrojích XaGUI; nekopíruj staré zakomentované alternativy ani nullable kontext do nového `@NotNull` API.
