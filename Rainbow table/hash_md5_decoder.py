import pandas as pd

data = pd.read_csv(r'./md5_rainbow_table.csv')

hashObjetivo = input('Ingresar hash: ')

resultado = data[data['hash'] == hashObjetivo]

if resultado.empty:
    print('No se encontró la contraseña')
else:
    print('Contraseña encontrada: ' + resultado.password)
