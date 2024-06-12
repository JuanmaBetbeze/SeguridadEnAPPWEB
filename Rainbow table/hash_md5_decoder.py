import pandas as pd

hashObjetivo = input('Ingresar hash: ')

for i in range(1, 12):
    file_name = f"./RainbowTables/md5_rainbow_table_part_{i}.csv"
    print(file_name)
    data = pd.read_csv(file_name, dtype={"password": "string", "hash": "string"})
    resultado = data[data['hash'] == hashObjetivo]
    
    if not resultado.empty:
        break

if resultado.empty:
    print('No se encontró la contraseña')
else:
    print('Contraseña encontrada: ' + resultado.password)
