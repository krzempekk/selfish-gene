# Selfish gene
### Projekt nr 1 z przedmiotu "Programowanie obiektowe"
### Autor: Kamil Krzempek

Parametry uruchomieniowe aplikacji zapisywane są w pliku *src/main/resources/parameters.json*. Oprócz parametrów wymaganych przez zadanie znajdują się tam też dodatkowe parametry:

| Parametr | Znaczenie |
| --- | --- |
| plantsGrowth | Przyrost dzienny roślin (tyle roślin wyrośnie na każdym z obszarów) |
| stepPause | Przerwa pomiędzy kolejnymi klatkami animacji (może być potem zmieniana bezpośrednio w aplikacji) |
| mapNumber | Ilość symulowanych map (przy większej liczbie może być konieczne ręczne dostowanie rozmiaru okna) |

Uwagi:
1. Istotę do śledzenia wybiera się przez kliknięcie na mapę. Śledzona istota otoczona jest czerwoną ramką.
2. Istoty z dominującym genomem można wskazać przez kliknięcie odpowiedniego przycisku. Są wtedy one otaczane żółtą ramką. Zaznaczenie jest usuwane przez naciśnięcie drugi raz tego samego przycisku.
3. Naciśnięcie przycisku "SAVE" zapisuje statystyki do pliku *stats.json* w głównym katalogu projektu. Zapisywany jest tam najczęściej występujący genom w historii jako liczności poszczególnych genów ("0:3, 1:5,..." oznacza że genom ten ma 3 geny 0, 5 genów 1 itd.)
4. Tabela reprezentująca genotyp wskazuje liczność genów w genotypach dominujących oraz wybranego zwierzęcia dla każdej z map. Dla genotypu dominującego wskazuje także jego liczność.
5. Projekt korzysta z mavena i bibliotek org.json i Guava.
6. Wszystkie sprite'y zostały zaczerpnięte z internetu i są własnością ich twórców. Sprite stworzenia chodzącego na mapie został stworzony za pomocą [South Park Avatar Generator](https://southpark.cc.com/avatar).