import { Constants } from 'central';

export const login = `${Constants.central.root}/login?redirect=${encodeURIComponent(Constants.notes.root)}`;
export const colors = Constants.colors;
